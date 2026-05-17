
package com.example.yharness.provider;

import com.example.yharness.context.AgentContext;

public interface AIProvider {
    
    String generate(AgentContext context);
    
    ProviderType getType();
    
    String getName();
}
