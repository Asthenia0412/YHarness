package com.yancy.yharness.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yharness")
public class AgentProperties {

    private AgentIdentity agent = new AgentIdentity();
    private ProviderConfig provider = new ProviderConfig();
    private ContextConfig context = new ContextConfig();
    private HooksConfig hooks = new HooksConfig();
    private ReActConfig react = new ReActConfig();
    private MemoryConfig memory = new MemoryConfig();
    private SchedulerConfig scheduler = new SchedulerConfig();
    private EvalConfig eval = new EvalConfig();
    private MockConfig mock = new MockConfig();

    @Data
    public static class AgentIdentity {
        private String agentId = "yharness-sales-agent-v1";
    }

    @Data
    public static class ProviderConfig {
        private String type = "MOCK";
        private String apiKey = "mock-api-key";
        private String baseUrl = "https://api.mock.com";
        private String model = "mock-model-v1";
        private int timeout = 30000;
        private int maxTokens = 4096;
        private double temperature = 0.7;
    }

    @Data
    public static class ContextConfig {
        private int maxMessages = 50;
        private boolean compressionEnabled = true;
        private int slidingWindowSize = 10;
        private int compressTriggerSize = 20;
    }

    @Data
    public static class HooksConfig {
        private boolean enabled = true;
    }

    @Data
    public static class ReActConfig {
        private int maxIterations = 10;
        private boolean enableThinking = true;
    }

    @Data
    public static class MemoryConfig {
        private int conversationTtlSeconds = 86400;
        private int storyTtlSeconds = 86400;
        private int checkpointTtlSeconds = 3600;
    }

    @Data
    public static class SchedulerConfig {
        private int pollIntervalMs = 5000;
        private int leaseTtlSeconds = 30;
        private int maxBatchSize = 100;
    }

    @Data
    public static class EvalConfig {
        private boolean enabled = true;
    }

    @Data
    public static class MockConfig {
        private boolean enabled = true;
    }
}