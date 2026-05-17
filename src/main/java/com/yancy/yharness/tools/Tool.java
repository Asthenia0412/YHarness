
package com.yancy.yharness.tools;

import com.yancy.yharness.context.AgentContext;

import java.util.Map;

public interface Tool {
    
    String getName();
    
    String getDescription();
    
    String execute(AgentContext context, Map<String, Object> arguments);
    
    com.yancy.yharness.context.ToolDefinition getDefinition();
}
