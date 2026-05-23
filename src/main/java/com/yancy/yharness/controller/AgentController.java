package com.yancy.yharness.controller;

import com.yancy.yharness.model.AgentResponse;
import com.yancy.yharness.pipeline.DispatchPipeline;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

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
    public AgentResponse chat(@RequestBody ChatRequest request) {
        String userId = request.getUserId() != null ? request.getUserId() : "default-user";
        String conversationId = request.getConversationId() != null
                ? request.getConversationId()
                : userId + "_default";

        return dispatchPipeline.dispatchChat(
                userId,
                request.getMessage(),
                conversationId
        );
    }

    @Data
    public static class ChatRequest {
        private String userId;
        private String conversationId;
        private String message;
        private String language;
    }
}