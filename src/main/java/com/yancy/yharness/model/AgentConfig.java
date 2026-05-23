package com.yancy.yharness.model;

import lombok.Data;

import java.util.List;

@Data
public class AgentConfig {
    private String agentId;
    private ModelConfig modelConfig;
    private MemoryConfig memoryConfig;
    private ContextConfig contextConfig;
    private List<String> hooks;
    private ToolRegistryConfig toolRegistry;
    private ReActConfig reactConfig;

    @Data
    public static class ModelConfig {
        private String provider;
        private String modelName;
        private double temperature;
        private int maxTokens;
    }

    @Data
    public static class MemoryConfig {
        private int maxConversationSessions;
    }

    @Data
    public static class ContextConfig {
        private boolean compressionEnabled;
        private int slidingWindowSize;
    }

    @Data
    public static class ToolRegistryConfig {
        private List<String> domains;
    }

    @Data
    public static class ReActConfig {
        private int maxIterations;
    }
}