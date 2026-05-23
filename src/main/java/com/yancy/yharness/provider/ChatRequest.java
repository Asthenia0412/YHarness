package com.yancy.yharness.provider;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private String systemPrompt;
    private List<ChatMessage> messages;
    private List<ToolDefinition> tools;
    private double temperature;
    private int maxTokens;
    private String model;
}