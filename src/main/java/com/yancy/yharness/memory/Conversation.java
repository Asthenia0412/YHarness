package com.yancy.yharness.memory;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private String conversationId;
    private String userId;
    private String channelAccountId;
    private String summary;
    private String stateDelta;
    private List<SessionSummary> recentSessions;
    private String earlySummary;
    private int version;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public Conversation() {
        this.recentSessions = new ArrayList<>();
    }

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
    public List<SessionSummary> getRecentSessions() { return recentSessions; }
    public void setRecentSessions(List<SessionSummary> recentSessions) { this.recentSessions = recentSessions; }
    public String getEarlySummary() { return earlySummary; }
    public void setEarlySummary(String earlySummary) { this.earlySummary = earlySummary; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class SessionSummary {
        private String sessionId;
        private SessionType sessionType;
        private String summary;
        private java.time.LocalDateTime createdAt;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public SessionType getSessionType() { return sessionType; }
        public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}