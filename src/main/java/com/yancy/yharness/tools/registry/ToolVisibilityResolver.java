package com.yancy.yharness.tools.registry;

import com.yancy.yharness.tools.ToolDefinition;

import java.util.List;

public interface ToolVisibilityResolver {
    List<ToolDefinition> resolveVisibleTools(com.yancy.yharness.context.AgentContext context);
}