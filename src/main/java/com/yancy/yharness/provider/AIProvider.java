
package com.yancy.yharness.provider;

import com.yancy.yharness.context.AgentContext;

public interface AIProvider {
    
    String generate(AgentContext context);
    
    ProviderType getType();
    
    String getName();
}
