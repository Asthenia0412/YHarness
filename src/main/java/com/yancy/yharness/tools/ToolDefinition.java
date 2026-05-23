package com.yancy.yharness.tools;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ToolDefinition {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
    private Map<String, Object> outputSchema;
    private String sideEffectLevel;
    private String authPolicy;
    private int timeoutMs;
    private String retryPolicy;
    private boolean idempotent;
    private String visibility;
    private String resultPolicy;
    private String domain;
    private List<String> tags;

    public ToolDefinition(String name, String description) {
        this.name = name;
        this.description = description;
    }
}