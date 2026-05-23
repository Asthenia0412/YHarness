package com.yancy.yharness.controller;

import com.yancy.yharness.core.Agent;
import com.yancy.yharness.model.AgentRequest;
import com.yancy.yharness.model.AgentResponse;
import com.yancy.yharness.model.TaskType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final Agent agent;

    public AgentController(Agent agent) {
        this.agent = agent;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return java.util.Map.of(
                "status", "UP",
                "agentId", agent.getConfig().getAgentId(),
                "modelProvider", agent.getConfig().getModelConfig().getProvider()
        );
    }

    @PostMapping("/chat")
    public AgentResponse chat(@RequestBody ChatRequest request) {
        AgentRequest agentRequest = new AgentRequest();
        agentRequest.setUserId(request.getUserId() != null ? request.getUserId() : "default-user");
        agentRequest.setConversationId(request.getConversationId());
        agentRequest.setUserMessage(request.getMessage());
        agentRequest.setTaskType(TaskType.INBOUND);
        agentRequest.setLanguageCode(request.getLanguage() != null ? request.getLanguage() : "en");
        agentRequest.setChannelId("api");
        agentRequest.setChannelAccountId("default");
        agentRequest.setTimezone("Asia/Bangkok");

        return agent.handle(agentRequest);
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return java.util.Map.of(
                "agentConfig", agent.getConfig(),
                "evalTarget", agent.getEvalTarget().id()
        );
    }

    public static class ChatRequest {
        private String userId;
        private String conversationId;
        private String message;
        private String language;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }
}