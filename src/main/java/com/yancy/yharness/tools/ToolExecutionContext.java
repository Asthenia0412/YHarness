package com.yancy.yharness.tools;

import java.time.LocalDateTime;

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
    private java.util.Map<String, Object> permissions;
    private String callSource;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getChannelAccountId() { return channelAccountId; }
    public void setChannelAccountId(String channelAccountId) { this.channelAccountId = channelAccountId; }
    public java.util.Map<String, Object> getPermissions() { return permissions; }
    public void setPermissions(java.util.Map<String, Object> permissions) { this.permissions = permissions; }
    public String getCallSource() { return callSource; }
    public void setCallSource(String callSource) { this.callSource = callSource; }
}