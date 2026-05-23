package com.yancy.yharness.model;

public class ToolCallRecord {
    private String toolName;
    private String arguments;
    private String result;
    private boolean success;
    private long elapsedMs;

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getArguments() { return arguments; }
    public void setArguments(String arguments) { this.arguments = arguments; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
}