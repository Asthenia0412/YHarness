package com.yancy.yharness.model;

import lombok.Data;

import java.util.List;

@Data
public class AgentResponse {
    private String sessionId;
    private String finalReply;
    private List<ToolCallRecord> toolCalls;
    private TokenUsage tokenUsage;
    private long elapsedMs;
}