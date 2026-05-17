
package com.yancy.yharness.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentContext {
    
    private String systemPrompt;
    private List<Message> messages = new ArrayList<>();
    private List<ToolDefinition> toolDefinitions = new ArrayList<>();
    private LongTermMemory longTermMemory = new LongTermMemory();
    private String toolExecutionResult;
    private Map<String, Object> metadata = new HashMap<>();
    private String conversationId;
    
    // 业务状态存储（销售场景专用）
    private BusinessState businessState = new BusinessState();

    public AgentContext() {
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public List<ToolDefinition> getToolDefinitions() {
        return toolDefinitions;
    }

    public void setToolDefinitions(List<ToolDefinition> toolDefinitions) {
        this.toolDefinitions = toolDefinitions;
    }

    public void addToolDefinition(ToolDefinition toolDefinition) {
        this.toolDefinitions.add(toolDefinition);
    }

    public LongTermMemory getLongTermMemory() {
        return longTermMemory;
    }

    public void setLongTermMemory(LongTermMemory longTermMemory) {
        this.longTermMemory = longTermMemory;
    }

    public String getToolExecutionResult() {
        return toolExecutionResult;
    }

    public void setToolExecutionResult(String toolExecutionResult) {
        this.toolExecutionResult = toolExecutionResult;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void putMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public BusinessState getBusinessState() {
        return businessState;
    }

    public void setBusinessState(BusinessState businessState) {
        this.businessState = businessState;
    }
}
