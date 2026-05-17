
package com.example.yharness.tools;

import com.example.yharness.context.AgentContext;
import com.example.yharness.exception.AgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ToolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ToolExecutor.class);
    
    private final Map<String, Tool> tools = new HashMap<>();

    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
        logger.info("Registered tool: {}", tool.getName());
    }

    public Tool getTool(String name) {
        return tools.get(name);
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    public String execute(String toolName, AgentContext context, Map<String, Object> arguments) {
        Tool tool = tools.get(toolName);
        if (tool == null) {
            throw new AgentException("Tool not found: " + toolName);
        }
        
        logger.info("Executing tool: {} with args: {}", toolName, arguments);
        try {
            return tool.execute(context, arguments);
        } catch (Exception e) {
            logger.error("Error executing tool: {}", toolName, e);
            throw new AgentException("Error executing tool: " + toolName, e);
        }
    }

    public Map<String, Tool> getAllTools() {
        return new HashMap<>(tools);
    }
}
