package com.yancy.yharness.model;

public class TokenUsage {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    public TokenUsage() {}

    public TokenUsage(int promptTokens, int completionTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
    }

    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }

    public void add(TokenUsage other) {
        this.promptTokens += other.promptTokens;
        this.completionTokens += other.completionTokens;
        this.totalTokens += other.totalTokens;
    }
}