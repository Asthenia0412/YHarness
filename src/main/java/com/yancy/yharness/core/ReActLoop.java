package com.yancy.yharness.core;

import com.yancy.yharness.provider.*;
import com.yancy.yharness.model.ToolCallRecord;
import com.yancy.yharness.model.TokenUsage;

import java.util.ArrayList;
import java.util.List;

public class ReActLoop {
    private final ModelProvider modelProvider;
    private final int maxIterations;

    public ReActLoop(ModelProvider modelProvider, int maxIterations) {
        this.modelProvider = modelProvider;
        this.maxIterations = maxIterations;
    }

    public ReActResult execute(ReActInput input) {
        AgentState state = input.getState();
        List<ToolCallRecord> toolCallRecords = new ArrayList<>();
        int iterations = 0;

        ChatRequest request = buildChatRequest(input);

        while (iterations < maxIterations) {
            long stepStart = System.currentTimeMillis();

            ChatResponse response = modelProvider.chat(request);
            state.getPerfState().addTokenUsage(response.getTokenUsage());
            state.getPerfState().recordStepDuration(System.currentTimeMillis() - stepStart);

            if (response.isFinal()) {
                state.getOutputState().setFinalAnswer(response.getContent());
                state.getRuntimeState().addMessage("assistant: " + response.getContent());

                ReActResult result = new ReActResult();
                result.setFinalAnswer(response.getContent());
                result.setToolCallRecords(toolCallRecords);
                result.setTokenUsage(state.getPerfState().getTokenUsage());
                result.setIterations(iterations + 1);
                result.setFinishReason(response.getFinishReason());
                return result;
            }

            if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
                for (ToolCall toolCall : response.getToolCalls()) {
                    ToolCallRecord record = new ToolCallRecord();
                    record.setToolName(toolCall.getName());
                    record.setArguments(toolCall.getArguments());

                    String result = input.getToolExecutor().execute(
                            toolCall.getName(),
                            toolCall.getArguments(),
                            state
                    );
                    record.setResult(result);
                    record.setSuccess(true);
                    toolCallRecords.add(record);

                    state.getRuntimeState().addMessage("tool(" + toolCall.getName() + "): " + result);
                }
            }

            request = buildNextRequest(input, state);
            iterations++;
        }

        state.getOutputState().setFinalAnswer("I apologize, but I need more information to provide a complete answer. Could you please clarify your question?");
        ReActResult result = new ReActResult();
        result.setFinalAnswer(state.getOutputState().getFinalAnswer());
        result.setToolCallRecords(toolCallRecords);
        result.setTokenUsage(state.getPerfState().getTokenUsage());
        result.setIterations(iterations);
        result.setFinishReason("max_iterations");
        return result;
    }

    private ChatRequest buildChatRequest(ReActInput input) {
        ChatRequest request = new ChatRequest();
        request.setSystemPrompt(input.getSystemPrompt());
        request.setMessages(new ArrayList<>());
        request.getMessages().add(new ChatMessage("user", input.getUserMessage()));

        List<ToolDefinition> toolDefs = new ArrayList<>();
        if (input.getToolDefinitions() != null) {
            for (com.yancy.yharness.tools.ToolDefinition def : input.getToolDefinitions()) {
                ToolDefinition td = new ToolDefinition(def.getName(), def.getDescription());
                td.setInputSchema(def.getInputSchema());
                toolDefs.add(td);
            }
        }
        request.setTools(toolDefs);
        request.setTemperature(input.getTemperature());
        request.setMaxTokens(input.getMaxTokens());
        return request;
    }

    private ChatRequest buildNextRequest(ReActInput input, AgentState state) {
        ChatRequest request = new ChatRequest();
        request.setSystemPrompt(input.getSystemPrompt());
        request.setMessages(new ArrayList<>());
        request.getMessages().add(new ChatMessage("user", input.getUserMessage()));

        for (String msg : state.getRuntimeState().getRuntimeMessages()) {
            String[] parts = msg.split(":\\s*", 2);
            String role = parts[0];
            String content = parts.length > 1 ? parts[1] : "";
            switch (role) {
                case "assistant":
                    request.getMessages().add(new ChatMessage("assistant", content));
                    break;
                case "tool":
                    request.getMessages().add(new ChatMessage("tool", content));
                    break;
                default:
                    request.getMessages().add(new ChatMessage("user", content));
            }
        }

        List<ToolDefinition> toolDefs = new ArrayList<>();
        if (input.getToolDefinitions() != null) {
            for (com.yancy.yharness.tools.ToolDefinition def : input.getToolDefinitions()) {
                ToolDefinition td = new ToolDefinition(def.getName(), def.getDescription());
                td.setInputSchema(def.getInputSchema());
                toolDefs.add(td);
            }
        }
        request.setTools(toolDefs);
        request.setTemperature(input.getTemperature());
        request.setMaxTokens(input.getMaxTokens());
        return request;
    }

    public static class ReActInput {
        private AgentState state;
        private String systemPrompt;
        private String userMessage;
        private List<com.yancy.yharness.tools.ToolDefinition> toolDefinitions;
        private ToolExecutor toolExecutor;
        private double temperature;
        private int maxTokens;

        public AgentState getState() { return state; }
        public void setState(AgentState state) { this.state = state; }
        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
        public String getUserMessage() { return userMessage; }
        public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
        public List<com.yancy.yharness.tools.ToolDefinition> getToolDefinitions() { return toolDefinitions; }
        public void setToolDefinitions(List<com.yancy.yharness.tools.ToolDefinition> toolDefinitions) { this.toolDefinitions = toolDefinitions; }
        public ToolExecutor getToolExecutor() { return toolExecutor; }
        public void setToolExecutor(ToolExecutor toolExecutor) { this.toolExecutor = toolExecutor; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    }

    public static class ReActResult {
        private String finalAnswer;
        private List<ToolCallRecord> toolCallRecords;
        private TokenUsage tokenUsage;
        private int iterations;
        private String finishReason;

        public String getFinalAnswer() { return finalAnswer; }
        public void setFinalAnswer(String finalAnswer) { this.finalAnswer = finalAnswer; }
        public List<ToolCallRecord> getToolCallRecords() { return toolCallRecords; }
        public void setToolCallRecords(List<ToolCallRecord> toolCallRecords) { this.toolCallRecords = toolCallRecords; }
        public TokenUsage getTokenUsage() { return tokenUsage; }
        public void setTokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; }
        public int getIterations() { return iterations; }
        public void setIterations(int iterations) { this.iterations = iterations; }
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }
}