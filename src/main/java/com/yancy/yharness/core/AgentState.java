package com.yancy.yharness.core;

import com.yancy.yharness.model.AgentRequest;
import com.yancy.yharness.model.TokenUsage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AgentState {
    private AgentInputState inputState;
    private AgentRuntimeState runtimeState;
    private AgentOutputState outputState;
    private AgentPerformanceState perfState;

    public AgentState() {
        this.runtimeState = new AgentRuntimeState();
        this.outputState = new AgentOutputState();
        this.perfState = new AgentPerformanceState();
    }

    public void initFromRequest(AgentRequest request) {
        this.inputState = new AgentInputState(request);
        this.perfState.setStartTime(System.currentTimeMillis());
    }

    public AgentInputState getInputState() { return inputState; }
    public AgentRuntimeState getRuntimeState() { return runtimeState; }
    public AgentOutputState getOutputState() { return outputState; }
    public AgentPerformanceState getPerfState() { return perfState; }

    public static class AgentInputState {
        private final AgentRequest request;

        public AgentInputState(AgentRequest request) {
            this.request = request;
        }

        public AgentRequest getRequest() { return request; }
        public String getUserId() { return request.getUserId(); }
        public String getConversationId() { return request.getConversationId(); }
        public String getUserMessage() { return request.getUserMessage(); }
        public com.yancy.yharness.model.TaskType getTaskType() { return request.getTaskType(); }
        public String getLanguageCode() { return request.getLanguageCode(); }
        public String getChannelAccountId() { return request.getChannelAccountId(); }
    }

    public static class AgentRuntimeState {
        private final List<String> runtimeMessages = new ArrayList<>();
        private final java.util.Map<String, Object> attachmentCache = new java.util.HashMap<>();
        private final List<String> warnings = new ArrayList<>();

        public void addMessage(String message) { runtimeMessages.add(message); }
        public List<String> getRuntimeMessages() { return runtimeMessages; }
        public void cacheAttachment(String key, Object value) { attachmentCache.put(key, value); }
        public Object getAttachment(String key) { return attachmentCache.get(key); }
        public void addWarning(String warning) { warnings.add(warning); }
        public List<String> getWarnings() { return warnings; }
    }

    public static class AgentOutputState {
        private String finalAnswer;
        private String handoffResult;
        private String evaluationResult;
        private String finishTaskResult;

        public void setFinalAnswer(String finalAnswer) { this.finalAnswer = finalAnswer; }
        public String getFinalAnswer() { return finalAnswer; }
        public String getHandoffResult() { return handoffResult; }
        public void setHandoffResult(String handoffResult) { this.handoffResult = handoffResult; }
        public String getEvaluationResult() { return evaluationResult; }
        public void setEvaluationResult(String evaluationResult) { this.evaluationResult = evaluationResult; }
        public String getFinishTaskResult() { return finishTaskResult; }
        public void setFinishTaskResult(String finishTaskResult) { this.finishTaskResult = finishTaskResult; }
    }

    public static class AgentPerformanceState {
        private TokenUsage tokenUsage = new TokenUsage();
        private long startTime;
        private final List<Long> stepDurations = new ArrayList<>();

        public TokenUsage getTokenUsage() { return tokenUsage; }
        public void setTokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; }
        public void addTokenUsage(TokenUsage additional) { this.tokenUsage.add(additional); }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public void recordStepDuration(long durationMs) { stepDurations.add(durationMs); }
        public List<Long> getStepDurations() { return stepDurations; }
        public long getElapsedMs() { return System.currentTimeMillis() - startTime; }
    }
}