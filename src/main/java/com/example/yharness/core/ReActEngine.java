
package com.example.yharness.core;

import com.example.yharness.context.AgentContext;
import com.example.yharness.context.Message;
import com.example.yharness.context.MessageRole;
import com.example.yharness.context.ToolDefinition;
import com.example.yharness.config.AgentProperties;
import com.example.yharness.exception.AgentException;
import com.example.yharness.hooks.HookManager;
import com.example.yharness.provider.AIProvider;
import com.example.yharness.provider.ProviderFactory;
import com.example.yharness.tools.Tool;
import com.example.yharness.tools.ToolExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ReActEngine {

    private static final Logger logger = LoggerFactory.getLogger(ReActEngine.class);
    
    private final ProviderFactory providerFactory;
    private final ToolExecutor toolExecutor;
    private final HookManager hookManager;
    private final AgentProperties agentProperties;
    private final ObjectMapper objectMapper;

    public ReActEngine(ProviderFactory providerFactory, 
                      ToolExecutor toolExecutor,
                      HookManager hookManager,
                      AgentProperties agentProperties,
                      ObjectMapper objectMapper) {
        this.providerFactory = providerFactory;
        this.toolExecutor = toolExecutor;
        this.hookManager = hookManager;
        this.agentProperties = agentProperties;
        this.objectMapper = objectMapper;
    }

    public String start(AgentContext context, String userInput) {
        long startTime = System.currentTimeMillis();
        
        hookManager.onReActStart(context);
        hookManager.onMessageReceived(context, userInput);
        
        context.addMessage(new Message(MessageRole.USER, userInput));
        
        int iteration = 0;
        String finalResponse = "";
        
        while (iteration < agentProperties.getReact().getMaxIterations()) {
            iteration++;
            logger.debug("ReAct iteration {} starting", iteration);
            
            Thought thought = process(context);
            
            if (thought.isFinal()) {
                finalResponse = thought.getContent();
                break;
            }
            
            if (thought.getAction() != null && thought.getAction().isToolCall()) {
                executeTool(context, thought.getAction());
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        hookManager.onReActEnd(context, duration);
        hookManager.onMessageSend(context, finalResponse);
        
        context.addMessage(new Message(MessageRole.ASSISTANT, finalResponse));
        
        return finalResponse;
    }

    public Thought process(AgentContext context) {
        hookManager.onProviderCall(context);
        
        AIProvider provider = providerFactory.getProvider();
        String response = provider.generate(context);
        
        hookManager.onProviderResponse(context, response);
        
        Thought thought = parseResponse(response);
        hookManager.onThoughtGenerated(context, thought);
        
        return thought;
    }

    private Thought parseResponse(String response) {
        Thought thought = new Thought();
        
        if (response == null || response.isEmpty()) {
            thought.setContent("无法获取响应");
            thought.setAction(new Action(ActionType.FINISH));
            return thought;
        }
        
        try {
            JsonNode root = objectMapper.readTree(response);
            
            if (root.has("tool_calls")) {
                JsonNode toolCalls = root.get("tool_calls");
                if (toolCalls.isArray() && toolCalls.size() > 0) {
                    JsonNode toolCall = toolCalls.get(0);
                    JsonNode function = toolCall.get("function");
                    
                    String toolName = function.get("name").asText();
                    String argsJson = function.get("arguments").toString();
                    
                    Map<String, Object> arguments = objectMapper.readValue(
                        argsJson, 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                    );
                    
                    Action action = new Action(ActionType.TOOL_CALL);
                    action.setToolName(toolName);
                    action.setArguments(arguments);
                    
                    thought.setContent("需要调用工具: " + toolName);
                    thought.setAction(action);
                    return thought;
                }
            }
            
            if (root.has("content")) {
                thought.setContent(root.get("content").asText());
                thought.setAction(new Action(ActionType.FINISH));
                return thought;
            }
        } catch (JsonProcessingException e) {
            logger.debug("Response is not JSON, treating as text: {}", response);
        }
        
        thought.setContent(response);
        thought.setAction(new Action(ActionType.FINISH));
        return thought;
    }

    private void executeTool(AgentContext context, Action action) {
        hookManager.onToolCall(context, action);
        
        String toolName = action.getToolName();
        Map<String, Object> arguments = action.getArguments();
        
        try {
            String result = toolExecutor.execute(toolName, context, arguments);
            context.setToolExecutionResult(result);
            
            context.addMessage(new Message(MessageRole.TOOL, result, toolName));
            
            hookManager.onToolResult(context, result);
            
        } catch (AgentException e) {
            logger.error("Tool execution failed", e);
            context.setToolExecutionResult("工具执行失败: " + e.getMessage());
            hookManager.onError(context, e);
        }
    }

    public void registerToolsToContext(AgentContext context) {
        Map<String, Tool> tools = toolExecutor.getAllTools();
        for (Tool tool : tools.values()) {
            context.addToolDefinition(tool.getDefinition());
        }
    }
}
