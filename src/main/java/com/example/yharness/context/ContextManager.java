
package com.example.yharness.context;

import com.example.yharness.config.AgentProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContextManager {

    private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);
    
    private final AgentProperties agentProperties;
    private final ObjectMapper objectMapper;
    private final Map<String, AgentContext> contextCache = new HashMap<>();

    public ContextManager(AgentProperties agentProperties, ObjectMapper objectMapper) {
        this.agentProperties = agentProperties;
        this.objectMapper = objectMapper;
    }

    public AgentContext createContext() {
        AgentContext context = new AgentContext();
        context.setSystemPrompt(agentProperties.getContext().getSystemPrompt());
        context.setConversationId(java.util.UUID.randomUUID().toString());
        contextCache.put(context.getConversationId(), context);
        logger.debug("Created new context with id: {}", context.getConversationId());
        return context;
    }

    public AgentContext getContext(String conversationId) {
        return contextCache.get(conversationId);
    }

    public void addMessage(String conversationId, Message message) {
        AgentContext context = contextCache.get(conversationId);
        if (context != null) {
            context.addMessage(message);
            trimMessages(context);
        }
    }

    private void trimMessages(AgentContext context) {
        int maxMessages = agentProperties.getContext().getMaxMessages();
        List<Message> messages = context.getMessages();
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    public void updateToolResult(String conversationId, String result) {
        AgentContext context = contextCache.get(conversationId);
        if (context != null) {
            context.setToolExecutionResult(result);
        }
    }

    public void addToolDefinition(String conversationId, ToolDefinition toolDefinition) {
        AgentContext context = contextCache.get(conversationId);
        if (context != null) {
            context.addToolDefinition(toolDefinition);
        }
    }

    public String getPromptForOpenAI(AgentContext context) {
        try {
            Map<String, Object> prompt = new HashMap<>();
            prompt.put("system", context.getSystemPrompt());
            
            if (context.getToolExecutionResult() != null) {
                prompt.put("tool_result", context.getToolExecutionResult());
            }
            
            if (!context.getLongTermMemory().getItems().isEmpty()) {
                prompt.put("memory", context.getLongTermMemory().getItems());
            }
            
            if (!context.getToolDefinitions().isEmpty()) {
                prompt.put("tools", context.getToolDefinitions());
            }
            
            return objectMapper.writeValueAsString(prompt);
        } catch (JsonProcessingException e) {
            logger.error("Error building OpenAI prompt", e);
            return context.getSystemPrompt();
        }
    }

    public String getPromptForAnthropic(AgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getSystemPrompt());
        
        if (!context.getToolDefinitions().isEmpty()) {
            sb.append("\n\n可用工具：\n");
            for (ToolDefinition tool : context.getToolDefinitions()) {
                sb.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
            }
        }
        
        if (context.getToolExecutionResult() != null) {
            sb.append("\n\n工具执行结果：\n").append(context.getToolExecutionResult());
        }
        
        return sb.toString();
    }

    public void clear(String conversationId) {
        contextCache.remove(conversationId);
    }

    public void clearAll() {
        contextCache.clear();
    }
}
