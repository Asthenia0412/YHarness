package com.yancy.yharness.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class ProviderFactory {
    private final Map<String, ModelProvider> providers = new ConcurrentHashMap<>();

    public void register(ModelProvider provider) {
        providers.put(provider.getName(), provider);
    }

    public ModelProvider getProvider(String type) {
        return providers.values().stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No provider found for type: " + type));
    }

    public ModelProvider getProviderByName(String name) {
        ModelProvider provider = providers.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("No provider found with name: " + name);
        }
        return provider;
    }
}