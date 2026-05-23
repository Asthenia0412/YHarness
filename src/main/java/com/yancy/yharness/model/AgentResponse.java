package com.yancy.yharness.model;

import java.util.List;

public class AgentResponse {
    private String sessionId;
    private String finalReply;
    private List<ToolCallRecord> toolCalls;
    private TokenUsage tokenUsage;
    private long elapsedMs;

    public AgentResponse() {}

    public AgentResponse(String sessionId, String finalReply) {
        this.sessionId = sessionId;
        this.finalReply = finalReply;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getFinalReply() { return finalReply; }
    public void setFinalReply(String finalReply) { this.finalReply = finalReply; }
    public List<ToolCallRecord> getToolCalls() { return toolCalls; }
    public void setToolCalls(List<ToolCallRecord> toolCalls) { this.toolCalls = toolCalls; }
    public TokenUsage getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
}