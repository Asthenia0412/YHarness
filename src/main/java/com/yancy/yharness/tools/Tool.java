package com.yancy.yharness.tools;

import java.util.Map;

public interface Tool {
    String getName();
    String getDescription();
    ToolDefinition getDefinition();
    ToolResult execute(ToolExecutionContext context, Map<String, Object> arguments);
}