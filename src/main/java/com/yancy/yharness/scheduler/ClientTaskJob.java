package com.yancy.yharness.scheduler;

import java.time.LocalDateTime;

public class ClientTaskJob {
    private Long id;
    private String taskKey;
    private String taskName;
    private String userId;
    private String channelAccountId;
    private String accountId;
    private String status;
    private LocalDateTime dueAt;
    private LocalDateTime leasedUntil;
    private String leaseHolder;
    private int retryCount;
    private String strategySummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaskKey() { return taskKey; }
    public void setTaskKey(String taskKey) { this.taskKey = taskKey; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getChannelAccountId() { return channelAccountId; }
    public void setChannelAccountId(String channelAccountId) { this.channelAccountId = channelAccountId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
    public LocalDateTime getLeasedUntil() { return leasedUntil; }
    public void setLeasedUntil(LocalDateTime leasedUntil) { this.leasedUntil = leasedUntil; }
    public String getLeaseHolder() { return leaseHolder; }
    public void setLeaseHolder(String leaseHolder) { this.leaseHolder = leaseHolder; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public String getStrategySummary() { return strategySummary; }
    public void setStrategySummary(String strategySummary) { this.strategySummary = strategySummary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}