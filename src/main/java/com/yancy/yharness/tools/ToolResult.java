package com.yancy.yharness.tools;

public class ToolResult {
    private boolean success;
    private String code;
    private String message;
    private Object data;
    private boolean retryable;
    private boolean userVisible;
    private String summary;

    public ToolResult() {}

    public static ToolResult success(Object data, String summary) {
        ToolResult result = new ToolResult();
        result.setSuccess(true);
        result.setCode("OK");
        result.setData(data);
        result.setSummary(summary);
        result.setRetryable(false);
        result.setUserVisible(true);
        return result;
    }

    public static ToolResult failure(String code, String message, boolean retryable) {
        ToolResult result = new ToolResult();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        result.setRetryable(retryable);
        result.setUserVisible(true);
        return result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public boolean isRetryable() { return retryable; }
    public void setRetryable(boolean retryable) { this.retryable = retryable; }
    public boolean isUserVisible() { return userVisible; }
    public void setUserVisible(boolean userVisible) { this.userVisible = userVisible; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}