package com.yancy.yharness.memory;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Conversation {
    private String conversationId;
    private String userId;
    private String channelAccountId;
    private String summary;
    private String stateDelta;
    private List<SessionSummary> recentSessions = new ArrayList<>();
    private String earlySummary;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class SessionSummary {
        private String sessionId;
        private SessionType sessionType;
        private String summary;
        private LocalDateTime createdAt;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public SessionType getSessionType() { return sessionType; }
        public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}