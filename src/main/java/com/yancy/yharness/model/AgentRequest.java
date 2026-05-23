package com.yancy.yharness.model;

import lombok.Data;

import java.util.Map;

@Data
public class AgentRequest {
    private String userId;
    private String conversationId;
    private String userMessage;
    private TaskType taskType;
    private String languageCode;
    private String channelId;
    private String channelAccountId;
    private String accountId;
    private String timezone;
    private Map<String, Object> metadata;
}