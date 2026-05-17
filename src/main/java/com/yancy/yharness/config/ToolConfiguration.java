
package com.yancy.yharness.config;

import com.yancy.yharness.hooks.AgentHook;
import com.yancy.yharness.hooks.HookManager;
import com.yancy.yharness.tools.Tool;
import com.yancy.yharness.tools.ToolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ToolConfiguration implements CommandLineRunner {

    private final ToolExecutor toolExecutor;
    private final List<Tool> tools;
    private final HookManager hookManager;
    private final List<AgentHook> agentHooks;

    @Autowired(required = false)
    public ToolConfiguration(ToolExecutor toolExecutor, 
                            List<Tool> tools,
                            HookManager hookManager,
                            List<AgentHook> agentHooks) {
        this.toolExecutor = toolExecutor;
        this.tools = tools != null ? tools : java.util.Collections.emptyList();
        this.hookManager = hookManager;
        this.agentHooks = agentHooks != null ? agentHooks : java.util.Collections.emptyList();
    }

    @Override
    public void run(String... args) throws Exception {
        for (Tool tool : tools) {
            toolExecutor.registerTool(tool);
        }
        
        for (AgentHook hook : agentHooks) {
            hookManager.registerHook(hook);
        }
    }
}
