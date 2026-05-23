package com.yancy.yharness.model;

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
    private java.util.Map<String, Object> metadata;

    public AgentRequest() {}

    public AgentRequest(String userId, String conversationId, String userMessage, TaskType taskType) {
        this.userId = userId;
        this.conversationId = conversationId;
        this.userMessage = userMessage;
        this.taskType = taskType;
        this.languageCode = "en";
        this.channelId = "whatsapp";
        this.channelAccountId = "default";
        this.timezone = "Asia/Bangkok";
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getChannelAccountId() { return channelAccountId; }
    public void setChannelAccountId(String channelAccountId) { this.channelAccountId = channelAccountId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public java.util.Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }
}