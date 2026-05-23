package com.yancy.yharness.core;

import com.yancy.yharness.core.AgentState.AgentRuntimeState;

import java.util.Map;

public interface ToolExecutor {
    String execute(String toolName, String argumentsJson, AgentState state);
}