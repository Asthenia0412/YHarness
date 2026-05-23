package com.yancy.yharness.tools;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ToolExecutionContext {
    private String userId;
    private String conversationId;
    private String sessionId;
    private String traceId;
    private String tenantId;
    private LocalDateTime requestTime;
    private String language;
    private String timezone;
    private String channelAccountId;
    private Map<String, Object> permissions;
    private String callSource;
}