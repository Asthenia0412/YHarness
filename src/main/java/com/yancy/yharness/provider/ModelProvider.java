package com.yancy.yharness.provider;

import com.yancy.yharness.model.TokenUsage;

public interface ModelProvider {
    ChatResponse chat(ChatRequest request);
    String getName();
    boolean supports(String providerType);
}