
package com.example.yharness.tools;

import com.example.yharness.context.AgentContext;

import java.util.Map;

public interface Tool {
    
    String getName();
    
    String getDescription();
    
    String execute(AgentContext context, Map<String, Object> arguments);
    
    com.example.yharness.context.ToolDefinition getDefinition();
}
