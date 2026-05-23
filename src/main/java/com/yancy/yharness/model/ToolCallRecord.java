package com.yancy.yharness.model;

import lombok.Data;

@Data
public class ToolCallRecord {
    private String toolName;
    private String arguments;
    private String result;
    private boolean success;
    private long elapsedMs;
}