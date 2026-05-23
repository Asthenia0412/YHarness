package com.yancy.yharness.tools.registry;

import com.yancy.yharness.tools.Tool;
import com.yancy.yharness.tools.ToolDefinition;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class BaseToolRegistry implements ToolRegistry {
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    @Override
    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    @Override
    public Collection<ToolDefinition> getAllTools() {
        return tools.values().stream()
                .map(Tool::getDefinition)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ToolDefinition> getToolsByDomain(String domain) {
        return tools.values().stream()
                .map(Tool::getDefinition)
                .filter(def -> domain.equals(def.getDomain()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ToolDefinition> getToolsByTags(Set<String> tags) {
        return tools.values().stream()
                .map(Tool::getDefinition)
                .filter(def -> def.getTags() != null && def.getTags().stream().anyMatch(tags::contains))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Tool> findByName(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    @Override
    public boolean contains(String name) {
        return tools.containsKey(name);
    }

    public void registerAll(List<Tool> toolList) {
        toolList.forEach(this::register);
    }
}