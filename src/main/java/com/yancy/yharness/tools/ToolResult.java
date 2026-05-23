package com.yancy.yharness.tools;

import lombok.Data;

@Data
public class ToolResult {
    private boolean success;
    private String code;
    private String message;
    private Object data;
    private boolean retryable;
    private boolean userVisible;
    private String summary;

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
}