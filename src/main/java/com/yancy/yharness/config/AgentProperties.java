package com.yancy.yharness.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "yharness")
public class AgentProperties {
    private AgentConfig agent = new AgentConfig();
    private ProviderConfig provider = new ProviderConfig();
    private ContextConfig context = new ContextConfig();
    private HooksConfig hooks = new HooksConfig();
    private ReActConfig react = new ReActConfig();
    private MemoryConfig memory = new MemoryConfig();
    private SchedulerConfig scheduler = new SchedulerConfig();
    private EvalConfig eval = new EvalConfig();
    private MockConfig mock = new MockConfig();

    public AgentConfig getAgent() { return agent; }
    public void setAgent(AgentConfig agent) { this.agent = agent; }
    public ProviderConfig getProvider() { return provider; }
    public void setProvider(ProviderConfig provider) { this.provider = provider; }
    public ContextConfig getContext() { return context; }
    public void setContext(ContextConfig context) { this.context = context; }
    public HooksConfig getHooks() { return hooks; }
    public void setHooks(HooksConfig hooks) { this.hooks = hooks; }
    public ReActConfig getReact() { return react; }
    public void setReact(ReActConfig react) { this.react = react; }
    public MemoryConfig getMemory() { return memory; }
    public void setMemory(MemoryConfig memory) { this.memory = memory; }
    public SchedulerConfig getScheduler() { return scheduler; }
    public void setScheduler(SchedulerConfig scheduler) { this.scheduler = scheduler; }
    public EvalConfig getEval() { return eval; }
    public void setEval(EvalConfig eval) { this.eval = eval; }
    public MockConfig getMock() { return mock; }
    public void setMock(MockConfig mock) { this.mock = mock; }

    public static class AgentConfig {
        private String agentId = "yharness-sales-agent-v1";
        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }
    }

    public static class ProviderConfig {
        private String type = "MOCK";
        private String apiKey = "mock-api-key";
        private String baseUrl = "https://api.mock.com";
        private String model = "mock-model-v1";
        private int timeout = 30000;
        private int maxTokens = 4096;
        private double temperature = 0.7;
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
    }

    public static class ContextConfig {
        private int maxMessages = 50;
        private boolean compressionEnabled = true;
        private int slidingWindowSize = 10;
        private int compressTriggerSize = 20;
        public int getMaxMessages() { return maxMessages; }
        public void setMaxMessages(int maxMessages) { this.maxMessages = maxMessages; }
        public boolean isCompressionEnabled() { return compressionEnabled; }
        public void setCompressionEnabled(boolean compressionEnabled) { this.compressionEnabled = compressionEnabled; }
        public int getSlidingWindowSize() { return slidingWindowSize; }
        public void setSlidingWindowSize(int slidingWindowSize) { this.slidingWindowSize = slidingWindowSize; }
        public int getCompressTriggerSize() { return compressTriggerSize; }
        public void setCompressTriggerSize(int compressTriggerSize) { this.compressTriggerSize = compressTriggerSize; }
    }

    public static class HooksConfig {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class ReActConfig {
        private int maxIterations = 10;
        private boolean enableThinking = true;
        public int getMaxIterations() { return maxIterations; }
        public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
        public boolean isEnableThinking() { return enableThinking; }
        public void setEnableThinking(boolean enableThinking) { this.enableThinking = enableThinking; }
    }

    public static class MemoryConfig {
        private int conversationTtlSeconds = 86400;
        private int storyTtlSeconds = 86400;
        private int checkpointTtlSeconds = 3600;
        public int getConversationTtlSeconds() { return conversationTtlSeconds; }
        public void setConversationTtlSeconds(int conversationTtlSeconds) { this.conversationTtlSeconds = conversationTtlSeconds; }
        public int getStoryTtlSeconds() { return storyTtlSeconds; }
        public void setStoryTtlSeconds(int storyTtlSeconds) { this.storyTtlSeconds = storyTtlSeconds; }
        public int getCheckpointTtlSeconds() { return checkpointTtlSeconds; }
        public void setCheckpointTtlSeconds(int checkpointTtlSeconds) { this.checkpointTtlSeconds = checkpointTtlSeconds; }
    }

    public static class SchedulerConfig {
        private int pollIntervalMs = 5000;
        private int leaseTtlSeconds = 30;
        private int maxBatchSize = 100;
        public int getPollIntervalMs() { return pollIntervalMs; }
        public void setPollIntervalMs(int pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
        public int getLeaseTtlSeconds() { return leaseTtlSeconds; }
        public void setLeaseTtlSeconds(int leaseTtlSeconds) { this.leaseTtlSeconds = leaseTtlSeconds; }
        public int getMaxBatchSize() { return maxBatchSize; }
        public void setMaxBatchSize(int maxBatchSize) { this.maxBatchSize = maxBatchSize; }
    }

    public static class EvalConfig {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class MockConfig {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}