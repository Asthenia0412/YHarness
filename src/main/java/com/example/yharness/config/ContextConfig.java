
package com.example.yharness.config;

public class ContextConfig {
    
    private String systemPrompt = "你是一位专业的销售顾问AI助手。";
    private int maxMessages = 50;

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }
}
