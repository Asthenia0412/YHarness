package com.yancy.yharness.provider;

import java.util.List;

public class ChatRequest {
    private String systemPrompt;
    private List<ChatMessage> messages;
    private List<ToolDefinition> tools;
    private double temperature;
    private int maxTokens;
    private String model;

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
    public List<ToolDefinition> getTools() { return tools; }
    public void setTools(List<ToolDefinition> tools) { this.tools = tools; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}