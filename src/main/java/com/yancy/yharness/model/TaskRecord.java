package com.yancy.yharness.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRecord {
    private String id;
    private String userId;
    private String conversationId;
    private String taskType;
    private String status;
    private String userMessage;
    private String finalReply;
    private String errorMessage;
    private long elapsedMs;
    private int toolCallCount;
    private int tokenUsage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_TIMEOUT = "TIMEOUT";

    public void markRunning() {
        this.status = STATUS_RUNNING;
    }

    public void markDone(String reply, long elapsed, int toolCalls, int tokens) {
        this.status = STATUS_DONE;
        this.finalReply = reply;
        this.elapsedMs = elapsed;
        this.toolCallCount = toolCalls;
        this.tokenUsage = tokens;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String error) {
        this.status = STATUS_FAILED;
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }

    public void markTimeout() {
        this.status = STATUS_TIMEOUT;
        this.completedAt = LocalDateTime.now();
    }
}