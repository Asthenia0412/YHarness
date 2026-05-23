package com.yancy.yharness.provider;

import java.util.List;

public class ToolDefinition {
    private String name;
    private String description;
    private java.util.Map<String, Object> inputSchema;

    public ToolDefinition() {}
    public ToolDefinition(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public java.util.Map<String, Object> getInputSchema() { return inputSchema; }
    public void setInputSchema(java.util.Map<String, Object> inputSchema) { this.inputSchema = inputSchema; }
}