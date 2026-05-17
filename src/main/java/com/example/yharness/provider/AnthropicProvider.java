
package com.example.yharness.provider;

import com.example.yharness.context.AgentContext;
import com.example.yharness.context.Message;
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
public class AnthropicProvider implements AIProvider {

    private static final Logger logger = LoggerFactory.getLogger(AnthropicProvider.class);
    
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    public AnthropicProvider(ObjectMapper objectMapper) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = System.getenv("AI_API_KEY");
        this.baseUrl = "https://api.anthropic.com/v1";
        this.model = "claude-3-sonnet-20240229";
        this.maxTokens = 4096;
        this.temperature = 0.7;
    }

    @Override
    public String generate(AgentContext context) {
        try {
            RequestBody body = RequestBody.create(buildRequestBody(context), MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                    .url(baseUrl + "/messages")
                    .header("x-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("anthropic-version", "2023-06-01")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ProviderException("Anthropic API request failed: " + response.code());
                }
                
                String responseBody = response.body().string();
                logger.debug("Anthropic response: {}", responseBody);
                return parseResponse(responseBody);
            }
        } catch (IOException e) {
            logger.error("Error calling Anthropic API", e);
            throw new ProviderException("Error calling Anthropic API", e);
        }
    }

    private String buildRequestBody(AgentContext context) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);

        List<Map<String, Object>> messages = new ArrayList<>();

        for (Message msg : context.getMessages()) {
            Map<String, Object> message = new HashMap<>();
            message.put("role", msg.getRole().getValue());
            message.put("content", msg.getContent());
            messages.add(message);
        }

        requestBody.put("messages", messages);

        String systemContent = buildSystemContent(context);
        if (!systemContent.isEmpty()) {
            requestBody.put("system", systemContent);
        }

        if (!context.getToolDefinitions().isEmpty()) {
            List<Map<String, Object>> tools = new ArrayList<>();
            for (ToolDefinition tool : context.getToolDefinitions()) {
                tools.add(convertToolToAnthropicFormat(tool));
            }
            requestBody.put("tools", tools);
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

    private Map<String, Object> convertToolToAnthropicFormat(ToolDefinition tool) {
        Map<String, Object> toolMap = new HashMap<>();
        toolMap.put("name", tool.getName());
        toolMap.put("description", tool.getDescription());
        
        Map<String, Object> inputSchema = new HashMap<>();
        inputSchema.put("type", "object");
        
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
        
        inputSchema.put("properties", properties);
        inputSchema.put("required", required);
        toolMap.put("input_schema", inputSchema);
        
        return toolMap;
    }

    private String parseResponse(String responseBody) throws JsonProcessingException {
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        
        if (content != null && !content.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (Map<String, Object> part : content) {
                String type = (String) part.get("type");
                if ("text".equals(type)) {
                    result.append(part.get("text"));
                } else if ("tool_use".equals(type)) {
                    result.append(objectMapper.writeValueAsString(part));
                }
            }
            return result.toString();
        }
        
        return "";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.ANTHROPIC;
    }

    @Override
    public String getName() {
        return "Anthropic";
    }
}
