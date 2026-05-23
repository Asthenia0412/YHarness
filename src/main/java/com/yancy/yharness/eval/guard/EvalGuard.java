package com.yancy.yharness.eval.guard;

import com.yancy.yharness.tools.ToolResult;

public class EvalGuard {
    private static final ThreadLocal<Boolean> evalMode = ThreadLocal.withInitial(() -> false);

    public static void setEvalMode(boolean enabled) {
        evalMode.set(enabled);
    }

    public static boolean isEvalMode() {
        return evalMode.get();
    }

    public static void clear() {
        evalMode.remove();
    }

    public ToolResult guardTool(String toolName, java.util.Map<String, Object> arguments, 
                                 ToolExecutor realExecutor) {
        if (!isEvalMode()) {
            return realExecutor.execute(toolName, arguments);
        }

        if (isWriteOperation(toolName)) {
            return mockRun(toolName, arguments);
        }

        return realExecutor.execute(toolName, arguments);
    }

    private boolean isWriteOperation(String toolName) {
        return false;
    }

    private ToolResult mockRun(String toolName, java.util.Map<String, Object> arguments) {
        return ToolResult.success(
                java.util.Collections.singletonMap("mock", true),
                "[EVAL MOCK] Tool " + toolName + " execution skipped in eval mode"
        );
    }

    @FunctionalInterface
    public interface ToolExecutor {
        ToolResult execute(String toolName, java.util.Map<String, Object> arguments);
    }
}