package com.yancy.yharness.model;

public class AgentConfig {
    private String agentId;
    private ModelConfig modelConfig;
    private MemoryConfig memoryConfig;
    private ContextConfig contextConfig;
    private java.util.List<String> hooks;
    private ToolRegistryConfig toolRegistry;
    private ReActConfig reactConfig;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public ModelConfig getModelConfig() { return modelConfig; }
    public void setModelConfig(ModelConfig modelConfig) { this.modelConfig = modelConfig; }
    public MemoryConfig getMemoryConfig() { return memoryConfig; }
    public void setMemoryConfig(MemoryConfig memoryConfig) { this.memoryConfig = memoryConfig; }
    public ContextConfig getContextConfig() { return contextConfig; }
    public void setContextConfig(ContextConfig contextConfig) { this.contextConfig = contextConfig; }
    public java.util.List<String> getHooks() { return hooks; }
    public void setHooks(java.util.List<String> hooks) { this.hooks = hooks; }
    public ToolRegistryConfig getToolRegistry() { return toolRegistry; }
    public void setToolRegistry(ToolRegistryConfig toolRegistry) { this.toolRegistry = toolRegistry; }
    public ReActConfig getReactConfig() { return reactConfig; }
    public void setReactConfig(ReActConfig reactConfig) { this.reactConfig = reactConfig; }

    public static class ModelConfig {
        private String provider;
        private String modelName;
        private double temperature;
        private int maxTokens;
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    }

    public static class MemoryConfig {
        private int maxConversationSessions;
        public int getMaxConversationSessions() { return maxConversationSessions; }
        public void setMaxConversationSessions(int maxConversationSessions) { this.maxConversationSessions = maxConversationSessions; }
    }

    public static class ContextConfig {
        private boolean compressionEnabled;
        private int slidingWindowSize;
        public boolean isCompressionEnabled() { return compressionEnabled; }
        public void setCompressionEnabled(boolean compressionEnabled) { this.compressionEnabled = compressionEnabled; }
        public int getSlidingWindowSize() { return slidingWindowSize; }
        public void setSlidingWindowSize(int slidingWindowSize) { this.slidingWindowSize = slidingWindowSize; }
    }

    public static class ToolRegistryConfig {
        private java.util.List<String> domains;
        public java.util.List<String> getDomains() { return domains; }
        public void setDomains(java.util.List<String> domains) { this.domains = domains; }
    }

    public static class ReActConfig {
        private int maxIterations;
        public int getMaxIterations() { return maxIterations; }
        public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
    }
}