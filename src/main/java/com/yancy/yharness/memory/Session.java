package com.yancy.yharness.memory;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}