
package com.example.yharness.core;

import java.util.HashMap;
import java.util.Map;

public class Action {
    
    private ActionType type;
    private String toolName;
    private Map<String, Object> arguments = new HashMap<>();
    private String finishReason;

    public Action() {
    }

    public Action(ActionType type) {
        this.type = type;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    public void addArgument(String key, Object value) {
        this.arguments.put(key, value);
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public boolean isToolCall() {
        return ActionType.TOOL_CALL.equals(type);
    }

    public boolean isFinish() {
        return ActionType.FINISH.equals(type);
    }
}
