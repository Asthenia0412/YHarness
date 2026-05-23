package com.yancy.yharness.hooks.impl;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.core.AgentState;
import com.yancy.yharness.hooks.AgentHook;
import com.yancy.yharness.hooks.HookType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsHook implements AgentHook {
    private static final Logger log = LoggerFactory.getLogger(MetricsHook.class);
    private final Map<String, AtomicLong> toolCallCounts = new ConcurrentHashMap<>();
    private final AtomicLong totalModelCalls = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    @Override
    public String getName() { return "MetricsHook"; }

    @Override
    public HookType getType() { return HookType.AFTER_TOOL_CALL; }

    @Override
    public void onSessionStart(AgentContext context, AgentState state) {
        log.info("[Metrics] Session started");
    }

    @Override
    public void onContextAssembling(AgentContext context) {}

    @Override
    public void onMemoryRetrieved(AgentContext context) {}

    @Override
    public void onBeforeModelCall(AgentContext context, AgentState state) {
        totalModelCalls.incrementAndGet();
    }

    @Override
    public void onAfterModelCall(AgentContext context, AgentState state, String modelResponse) {}

    @Override
    public void onBeforeToolCall(AgentContext context, AgentState state, String toolName) {
        toolCallCounts.computeIfAbsent(toolName, k -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public void onAfterToolCall(AgentContext context, AgentState state, String toolName, String result) {}

    @Override
    public void onBeforeSessionSummarize(AgentContext context, AgentState state) {}

    @Override
    public void onConversationUpdated(AgentContext context) {}

    @Override
    public void onStoryUpdated(AgentContext context) {}

    @Override
    public void onSessionEnd(AgentContext context, AgentState state) {
        log.info("[Metrics] Session ended - modelCalls: {}, toolCalls: {}, totalTokens: {}, elapsedMs: {}",
                totalModelCalls.get(), toolCallCounts.values().stream().mapToLong(AtomicLong::get).sum(),
                state.getPerfState().getTokenUsage().getTotalTokens(),
                state.getPerfState().getElapsedMs());
    }

    @Override
    public void onError(AgentContext context, AgentState state, Throwable error) {
        totalErrors.incrementAndGet();
        log.warn("[Metrics] Error count: {}", totalErrors.get());
    }

    public Map<String, Long> getToolCallCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        toolCallCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public long getTotalModelCalls() { return totalModelCalls.get(); }
    public long getTotalErrors() { return totalErrors.get(); }
}