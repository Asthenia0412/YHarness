
package com.yancy.yharness.config;

public class ContextConfig {
    
    private String systemPrompt = "你是一位专业的销售顾问AI助手。";
    private int maxMessages = 50;
    private int slidingWindowSize = 10;
    private int compressTriggerSize = 20;
    private boolean compressionEnabled = true;

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

    public int getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public void setSlidingWindowSize(int slidingWindowSize) {
        this.slidingWindowSize = slidingWindowSize;
    }

    public int getCompressTriggerSize() {
        return compressTriggerSize;
    }

    public void setCompressTriggerSize(int compressTriggerSize) {
        this.compressTriggerSize = compressTriggerSize;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }
}
