package com.yancy.yharness.tools.policy;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.tools.ToolDefinition;
import com.yancy.yharness.tools.registry.ToolVisibilityResolver;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PolicyToolFilter implements ToolVisibilityResolver {
    private final com.yancy.yharness.tools.catalog.DomainToolRegistry domainRegistry;

    public PolicyToolFilter(com.yancy.yharness.tools.catalog.DomainToolRegistry domainRegistry) {
        this.domainRegistry = domainRegistry;
    }

    @Override
    public List<ToolDefinition> resolveVisibleTools(AgentContext context) {
        Set<String> applicableDomains = resolveDomainsForContext(context);
        Collection<ToolDefinition> domainTools = domainRegistry.getToolsByDomains(applicableDomains);

        return domainTools.stream()
                .filter(tool -> applyPolicyFilters(tool, context))
                .collect(Collectors.toList());
    }

    private Set<String> resolveDomainsForContext(AgentContext context) {
        Set<String> domains = new java.util.HashSet<>();
        domains.add("crm");
        domains.add("advertising");
        domains.add("promotion");
        domains.add("knowledge");
        if (context.getUserMessage() != null && context.getUserMessage().toLowerCase().contains("stock")) {
            domains.add("inventory");
        }
        return domains;
    }

    private boolean applyPolicyFilters(ToolDefinition tool, AgentContext context) {
        return true;
    }
}