
package com.example.yharness.provider;

import com.example.yharness.context.AgentContext;
import com.example.yharness.context.Message;
import com.example.yharness.context.MessageRole;
import com.example.yharness.context.ToolDefinition;
import com.example.yharness.exception.ProviderException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIProvider implements AIProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);
    
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    public OpenAIProvider(ObjectMapper objectMapper) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = System.getenv("AI_API_KEY");
        this.baseUrl = "https://api.openai.com/v1";
        this.model = "gpt-4o-mini";
        this.maxTokens = 4096;
        this.temperature = 0.7;
    }

    @Override
    public String generate(AgentContext context) {
        try {
            RequestBody body = RequestBody.create(buildRequestBody(context), MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ProviderException("OpenAI API request failed: " + response.code());
                }
                
                String responseBody = response.body().string();
                logger.debug("OpenAI response: {}", responseBody);
                return parseResponse(responseBody);
            }
        } catch (IOException e) {
            logger.error("Error calling OpenAI API", e);
            throw new ProviderException("Error calling OpenAI API", e);
        }
    }

    private String buildRequestBody(AgentContext context) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);

        List<Map<String, Object>> messages = new ArrayList<>();
        
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSystemContent(context));
        messages.add(systemMessage);

        for (Message msg : context.getMessages()) {
            Map<String, Object> message = new HashMap<>();
            message.put("role", msg.getRole().getValue());
            message.put("content", msg.getContent());
            messages.add(message);
        }

        requestBody.put("messages", messages);

        if (!context.getToolDefinitions().isEmpty()) {
            List<Map<String, Object>> tools = new ArrayList<>();
            for (ToolDefinition tool : context.getToolDefinitions()) {
                tools.add(convertToolToOpenAIFormat(tool));
            }
            requestBody.put("tools", tools);
            requestBody.put("tool_choice", "auto");
        }

        return objectMapper.writeValueAsString(requestBody);
    }

    private String buildSystemContent(AgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getSystemPrompt());
        
        if (context.getToolExecutionResult() != null) {
            sb.append("\n\n工具执行结果：\n").append(context.getToolExecutionResult());
        }
        
        if (!context.getLongTermMemory().getItems().isEmpty()) {
            sb.append("\n\n长期记忆：\n");
            for (com.example.yharness.context.LongTermMemory.MemoryItem item : context.getLongTermMemory().getItems()) {
                sb.append("- ").append(item.getKey()).append(": ").append(item.getValue()).append("\n");
            }
        }
        
        return sb.toString();
    }

    private Map<String, Object> convertToolToOpenAIFormat(ToolDefinition tool) {
        Map<String, Object> toolMap = new HashMap<>();
        toolMap.put("type", "function");
        
        Map<String, Object> function = new HashMap<>();
        function.put("name", tool.getName());
        function.put("description", tool.getDescription());
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();
        
        for (com.example.yharness.context.ToolParameter param : tool.getParameters()) {
            Map<String, Object> prop = new HashMap<>();
            prop.put("type", param.getType());
            prop.put("description", param.getDescription());
            properties.put(param.getName(), prop);
            if (param.isRequired()) {
                required.add(param.getName());
            }
        }
        
        parameters.put("properties", properties);
        parameters.put("required", required);
        function.put("parameters", parameters);
        
        toolMap.put("function", function);
        return toolMap;
    }

    private String parseResponse(String responseBody) throws JsonProcessingException {
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            
            Object toolCalls = message.get("tool_calls");
            if (toolCalls != null) {
                return objectMapper.writeValueAsString(message);
            }
            
            return (String) message.get("content");
        }
        
        return "";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.OPENAI;
    }

    @Override
    public String getName() {
        return "OpenAI";
    }
}
