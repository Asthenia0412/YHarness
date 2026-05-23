package com.yancy.yharness.tools.registry;

import com.yancy.yharness.tools.Tool;
import com.yancy.yharness.tools.ToolDefinition;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ToolRegistry {
    void register(Tool tool);
    Collection<ToolDefinition> getAllTools();
    Collection<ToolDefinition> getToolsByDomain(String domain);
    Collection<ToolDefinition> getToolsByTags(Set<String> tags);
    Optional<Tool> findByName(String name);
    boolean contains(String name);
}