package com.yancy.yharness.tools.registry;

import com.yancy.yharness.tools.Tool;
import com.yancy.yharness.tools.ToolDefinition;
import com.yancy.yharness.tools.ToolExecutionContext;
import com.yancy.yharness.tools.ToolResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolDispatcher {
    private final BaseToolRegistry toolRegistry;
    private final Map<String, Long> invocationCount = new ConcurrentHashMap<>();

    public ToolDispatcher(BaseToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public ToolResult dispatch(String toolName, ToolExecutionContext context, Map<String, Object> arguments) {
        invocationCount.merge(toolName, 1L, Long::sum);

        return toolRegistry.findByName(toolName)
                .map(tool -> {
                    try {
                        return tool.execute(context, arguments);
                    } catch (Exception e) {
                        return ToolResult.failure("EXECUTION_ERROR",
                                "Tool execution failed: " + e.getMessage(), true);
                    }
                })
                .orElseGet(() -> ToolResult.failure("TOOL_NOT_FOUND",
                        "Tool not found: " + toolName, false));
    }

    public long getInvocationCount(String toolName) {
        return invocationCount.getOrDefault(toolName, 0L);
    }
}