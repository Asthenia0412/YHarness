package com.yancy.yharness.tools.catalog;

import com.yancy.yharness.tools.Tool;
import com.yancy.yharness.tools.ToolDefinition;
import com.yancy.yharness.tools.registry.BaseToolRegistry;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DomainToolRegistry {
    private final BaseToolRegistry baseRegistry;
    private final Map<String, Set<String>> domainMappings = new LinkedHashMap<>();

    public DomainToolRegistry(BaseToolRegistry baseRegistry) {
        this.baseRegistry = baseRegistry;
        initDefaultDomains();
    }

    private void initDefaultDomains() {
        domainMappings.put("crm", new HashSet<>());
        domainMappings.put("advertising", new HashSet<>());
        domainMappings.put("promotion", new HashSet<>());
        domainMappings.put("knowledge", new HashSet<>());
        domainMappings.put("inventory", new HashSet<>());
    }

    public void mapToolToDomain(String toolName, String domain) {
        domainMappings.computeIfAbsent(domain, k -> new HashSet<>()).add(toolName);
    }

    public Collection<ToolDefinition> getToolsByDomain(String domain) {
        Set<String> toolNames = domainMappings.getOrDefault(domain, Collections.emptySet());
        return toolNames.stream()
                .map(name -> baseRegistry.findByName(name).map(Tool::getDefinition).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Collection<ToolDefinition> getToolsByDomains(Set<String> domains) {
        return domains.stream()
                .flatMap(domain -> getToolsByDomain(domain).stream())
                .collect(Collectors.toList());
    }

    public Map<String, Set<String>> getDomainMappings() {
        return Collections.unmodifiableMap(domainMappings);
    }

    public void autoMapByDefinition() {
        baseRegistry.getAllTools().forEach(def -> {
            if (def.getDomain() != null) {
                domainMappings.computeIfAbsent(def.getDomain(), k -> new HashSet<>())
                        .add(def.getName());
            }
        });
    }
}