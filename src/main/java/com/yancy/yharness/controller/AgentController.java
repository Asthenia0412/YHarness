package com.yancy.yharness.controller;

import com.yancy.yharness.model.AgentResponse;
import com.yancy.yharness.model.TaskRecord;
import com.yancy.yharness.pipeline.DispatchPipeline;
import com.yancy.yharness.pipeline.DispatchResult;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final DispatchPipeline dispatchPipeline;

    public AgentController(DispatchPipeline dispatchPipeline) {
        this.dispatchPipeline = dispatchPipeline;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return java.util.Map.of(
                "status", "UP"
        );
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request) {
        String userId = request.getUserId() != null ? request.getUserId() : "default-user";
        String conversationId = request.getConversationId() != null
                ? request.getConversationId()
                : userId + "_default";

        DispatchResult result = dispatchPipeline.dispatchChat(
                userId,
                request.getMessage(),
                conversationId
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("taskId", result.getTaskId());
        response.put("status", result.getStatus());

        if (result.isDone() && result.getResponse() != null) {
            AgentResponse agentResp = result.getResponse();
            response.put("reply", agentResp.getFinalReply());
            response.put("sessionId", agentResp.getSessionId());
            response.put("elapsedMs", agentResp.getElapsedMs());
            if (agentResp.getTokenUsage() != null) {
                response.put("tokenUsage", agentResp.getTokenUsage().getTotalTokens());
            }
            if (agentResp.getToolCalls() != null) {
                response.put("toolCallCount", agentResp.getToolCalls().size());
            }
        }

        if (result.getErrorMessage() != null) {
            response.put("error", result.getErrorMessage());
        }

        return response;
    }

    @GetMapping("/result/{taskId}")
    public Map<String, Object> getResult(@PathVariable String taskId) {
        TaskRecord record = dispatchPipeline.getTaskResult(taskId);
        if (record == null) {
            return java.util.Map.of("error", "Task not found: " + taskId);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("taskId", record.getId());
        response.put("status", record.getStatus());
        response.put("userId", record.getUserId());
        response.put("createdAt", record.getCreatedAt());

        if (TaskRecord.STATUS_DONE.equals(record.getStatus())) {
            response.put("reply", record.getFinalReply());
            response.put("elapsedMs", record.getElapsedMs());
            response.put("toolCallCount", record.getToolCallCount());
            response.put("tokenUsage", record.getTokenUsage());
            response.put("completedAt", record.getCompletedAt());
        }

        if (TaskRecord.STATUS_FAILED.equals(record.getStatus()) || TaskRecord.STATUS_TIMEOUT.equals(record.getStatus())) {
            response.put("error", record.getErrorMessage());
        }

        return response;
    }

    @Data
    public static class ChatRequest {
        private String userId;
        private String conversationId;
        private String message;
        private String language;
    }
}