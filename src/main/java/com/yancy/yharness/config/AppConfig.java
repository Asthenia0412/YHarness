package com.yancy.yharness.config;

import com.yancy.yharness.hooks.AgentHook;
import com.yancy.yharness.hooks.HookManager;
import com.yancy.yharness.provider.MockProvider;
import com.yancy.yharness.provider.ProviderFactory;
import com.yancy.yharness.scheduler.Dispatcher;
import com.yancy.yharness.tools.Tool;
import com.yancy.yharness.tools.registry.BaseToolRegistry;
import com.yancy.yharness.tools.catalog.DomainToolRegistry;
import com.yancy.yharness.eval.EvalRegistry;
import com.yancy.yharness.eval.service.AgentEvalTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Configuration
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public MockProvider mockProvider() {
        return new MockProvider();
    }

    @Bean
    public ProviderFactory providerFactory(MockProvider mockProvider) {
        ProviderFactory factory = new ProviderFactory();
        factory.register(mockProvider);
        log.info("Registered MockProvider");
        return factory;
    }

    @Bean
    public HookManager hookManager(List<AgentHook> hooks) {
        HookManager manager = new HookManager();
        for (AgentHook hook : hooks) {
            manager.register(hook);
            log.info("Registered hook: {}", hook.getName());
        }
        return manager;
    }

    @Bean
    public EvalRegistry evalRegistry(AgentEvalTarget agentEvalTarget) {
        EvalRegistry registry = new EvalRegistry();
        registry.register(agentEvalTarget);
        log.info("Registered eval target: {}", agentEvalTarget.id());
        return registry;
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        org.springframework.context.ApplicationContext ctx = event.getApplicationContext();
        Dispatcher dispatcher = ctx.getBean(Dispatcher.class);
        AgentProperties agentProperties = ctx.getBean(AgentProperties.class);
        BaseToolRegistry toolRegistry = ctx.getBean(BaseToolRegistry.class);
        DomainToolRegistry domainRegistry = ctx.getBean(DomainToolRegistry.class);

        java.util.Collection<Tool> tools = ctx.getBeansOfType(Tool.class).values();
        toolRegistry.registerAll(tools.stream().toList());
        domainRegistry.autoMapByDefinition();

        dispatcher.start(agentProperties.getScheduler().getPollIntervalMs());
        log.info("Application ready - dispatcher started");
    }
}