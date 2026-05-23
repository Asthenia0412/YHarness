package com.yancy.yharness.eval.service;

import com.yancy.yharness.config.AgentProperties;
import com.yancy.yharness.eval.EvalInvokeResult;
import com.yancy.yharness.eval.EvalTarget;
import com.yancy.yharness.core.Agent;
import com.yancy.yharness.model.AgentRequest;
import com.yancy.yharness.model.AgentResponse;
import com.yancy.yharness.model.TaskType;
import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.eval.guard.EvalGuard;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AgentEvalTarget implements EvalTarget {
    private final ObjectProvider<Agent> agentProvider;
    private final String agentId;
    private final String agentName;

    public AgentEvalTarget(ObjectProvider<Agent> agentProvider, AgentProperties properties) {
        this.agentProvider = agentProvider;
        this.agentId = properties.getAgent().getAgentId();
        this.agentName = "SalesAgent Evaluation";
    }

    @Override
    public String id() {
        return agentId;
    }

    @Override
    public String name() {
        return agentName;
    }

    @Override
    public EvalInvokeResult invoke(AgentContext ctx, Map<String, Object> evalInput) {
        EvalGuard.setEvalMode(true);
        try {
            String userMessage = (String) evalInput.getOrDefault("userMessage", "");
            String userId = (String) evalInput.getOrDefault("userId", "eval-user");
            String conversationId = (String) evalInput.getOrDefault("conversationId", UUID.randomUUID().toString());

            AgentRequest request = new AgentRequest();
            request.setUserId(userId);
            request.setConversationId(conversationId);
            request.setUserMessage(userMessage);
            request.setTaskType(TaskType.INBOUND);

            Agent agent = agentProvider.getObject();

            long start = System.currentTimeMillis();
            AgentResponse response = agent.handle(request);
            long elapsed = System.currentTimeMillis() - start;

            EvalInvokeResult result = new EvalInvokeResult();
            result.setSuccess(true);
            result.setOutput(response.getFinalReply());
            result.setTraceId(response.getSessionId());

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("elapsedMs", elapsed);
            metrics.put("tokenUsage", response.getTokenUsage());
            metrics.put("toolCallCount", response.getToolCalls() != null ? response.getToolCalls().size() : 0);
            result.setMetrics(metrics);

            return result;
        } finally {
            EvalGuard.clear();
        }
    }
}