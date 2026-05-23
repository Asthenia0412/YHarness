package com.yancy.yharness.tools;

import java.util.List;
import java.util.Map;

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

    public ToolDefinition() {}

    public ToolDefinition(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, Object> getInputSchema() { return inputSchema; }
    public void setInputSchema(Map<String, Object> inputSchema) { this.inputSchema = inputSchema; }
    public Map<String, Object> getOutputSchema() { return outputSchema; }
    public void setOutputSchema(Map<String, Object> outputSchema) { this.outputSchema = outputSchema; }
    public String getSideEffectLevel() { return sideEffectLevel; }
    public void setSideEffectLevel(String sideEffectLevel) { this.sideEffectLevel = sideEffectLevel; }
    public String getAuthPolicy() { return authPolicy; }
    public void setAuthPolicy(String authPolicy) { this.authPolicy = authPolicy; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    public boolean isIdempotent() { return idempotent; }
    public void setIdempotent(boolean idempotent) { this.idempotent = idempotent; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getResultPolicy() { return resultPolicy; }
    public void setResultPolicy(String resultPolicy) { this.resultPolicy = resultPolicy; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}