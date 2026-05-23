package com.yancy.yharness.memory;

import java.time.LocalDateTime;

public class Session {
    private String sessionId;
    private String conversationId;
    private String userId;
    private String channelAccountId;
    private String summary;
    private String stateDelta;
    private SessionType sessionType;
    private String inputMessage;
    private String finalReply;
    private int toolCallCount;
    private int tokenUsage;
    private long elapsedMs;
    private LocalDateTime createdAt;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getChannelAccountId() { return channelAccountId; }
    public void setChannelAccountId(String channelAccountId) { this.channelAccountId = channelAccountId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getStateDelta() { return stateDelta; }
    public void setStateDelta(String stateDelta) { this.stateDelta = stateDelta; }
    public SessionType getSessionType() { return sessionType; }
    public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
    public String getInputMessage() { return inputMessage; }
    public void setInputMessage(String inputMessage) { this.inputMessage = inputMessage; }
    public String getFinalReply() { return finalReply; }
    public void setFinalReply(String finalReply) { this.finalReply = finalReply; }
    public int getToolCallCount() { return toolCallCount; }
    public void setToolCallCount(int toolCallCount) { this.toolCallCount = toolCallCount; }
    public int getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(int tokenUsage) { this.tokenUsage = tokenUsage; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}