package com.yancy.yharness.provider;

import com.yancy.yharness.model.TokenUsage;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String content;
    private List<ToolCall> toolCalls;
    private boolean isFinal;
    private String finishReason;
    private TokenUsage tokenUsage;
}