package com.yancy.yharness.scheduler;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}