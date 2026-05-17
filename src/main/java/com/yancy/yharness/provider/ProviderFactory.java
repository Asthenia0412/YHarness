
package com.yancy.yharness.provider;

import com.yancy.yharness.config.AgentProperties;
import com.yancy.yharness.exception.AgentException;
import org.springframework.stereotype.Component;

@Component
public class ProviderFactory {

    private final OpenAIProvider openAIProvider;
    private final AnthropicProvider anthropicProvider;
    private final AgentProperties agentProperties;

    public ProviderFactory(OpenAIProvider openAIProvider, 
                          AnthropicProvider anthropicProvider,
                          AgentProperties agentProperties) {
        this.openAIProvider = openAIProvider;
        this.anthropicProvider = anthropicProvider;
        this.agentProperties = agentProperties;
    }

    public AIProvider getProvider() {
        String type = agentProperties.getProvider().getType();
        return getProvider(ProviderType.fromValue(type));
    }

    public AIProvider getProvider(ProviderType type) {
        switch (type) {
            case OPENAI:
                return openAIProvider;
            case ANTHROPIC:
                return anthropicProvider;
            default:
                throw new AgentException("Unsupported provider type: " + type);
        }
    }
}
