package com.yancy.yharness.pipeline;

import com.yancy.yharness.model.AgentResponse;

public class DispatchResult {
    private final String taskId;
    private final String status;
    private final AgentResponse response;
    private final String errorMessage;

    private DispatchResult(String taskId, String status, AgentResponse response, String errorMessage) {
        this.taskId = taskId;
        this.status = status;
        this.response = response;
        this.errorMessage = errorMessage;
    }

    public static DispatchResult done(String taskId, AgentResponse response) {
        return new DispatchResult(taskId, "DONE", response, null);
    }

    public static DispatchResult pending(String taskId) {
        return new DispatchResult(taskId, "PENDING", null, null);
    }

    public static DispatchResult failed(String taskId, String error) {
        return new DispatchResult(taskId, "FAILED", null, error);
    }

    public String getTaskId() { return taskId; }
    public String getStatus() { return status; }
    public AgentResponse getResponse() { return response; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isDone() { return "DONE".equals(status); }
    public boolean isPending() { return "PENDING".equals(status); }
}