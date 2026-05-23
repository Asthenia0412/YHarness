package com.yancy.yharness.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenUsage {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    public TokenUsage() {}

    public TokenUsage(int promptTokens, int completionTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
    }

    public void add(TokenUsage other) {
        this.promptTokens += other.promptTokens;
        this.completionTokens += other.completionTokens;
        this.totalTokens += other.totalTokens;
    }
}