
package com.yancy.yharness.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yharness")
public class AgentProperties {
    
    private ProviderConfig provider = new ProviderConfig();
    private ContextConfig context = new ContextConfig();
    private HooksConfig hooks = new HooksConfig();
    private ReActConfig react = new ReActConfig();

    public ProviderConfig getProvider() {
        return provider;
    }

    public void setProvider(ProviderConfig provider) {
        this.provider = provider;
    }

    public ContextConfig getContext() {
        return context;
    }

    public void setContext(ContextConfig context) {
        this.context = context;
    }

    public HooksConfig getHooks() {
        return hooks;
    }

    public void setHooks(HooksConfig hooks) {
        this.hooks = hooks;
    }

    public ReActConfig getReact() {
        return react;
    }

    public void setReact(ReActConfig react) {
        this.react = react;
    }
}
