package com.yancy.yharness.hooks;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.core.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HookManager {
    private static final Logger log = LoggerFactory.getLogger(HookManager.class);
    private final List<AgentHook> hooks = new ArrayList<>();

    public void register(AgentHook hook) {
        hooks.add(hook);
        hooks.sort(Comparator.comparing(h -> h.getName()));
    }

    public void onSessionStart(AgentContext context, AgentState state) {
        hooks.forEach(h -> safeCall(() -> h.onSessionStart(context, state), h.getName(), "onSessionStart"));
    }

    public void onContextAssembling(AgentContext context) {
        hooks.forEach(h -> safeCall(() -> h.onContextAssembling(context), h.getName(), "onContextAssembling"));
    }

    public void onMemoryRetrieved(AgentContext context) {
        hooks.forEach(h -> safeCall(() -> h.onMemoryRetrieved(context), h.getName(), "onMemoryRetrieved"));
    }

    public void onBeforeModelCall(AgentContext context, AgentState state) {
        hooks.forEach(h -> safeCall(() -> h.onBeforeModelCall(context, state), h.getName(), "onBeforeModelCall"));
    }

    public void onAfterModelCall(AgentContext context, AgentState state, String modelResponse) {
        hooks.forEach(h -> safeCall(() -> h.onAfterModelCall(context, state, modelResponse), h.getName(), "onAfterModelCall"));
    }

    public void onBeforeToolCall(AgentContext context, AgentState state, String toolName) {
        hooks.forEach(h -> safeCall(() -> h.onBeforeToolCall(context, state, toolName), h.getName(), "onBeforeToolCall"));
    }

    public void onAfterToolCall(AgentContext context, AgentState state, String toolName, String result) {
        hooks.forEach(h -> safeCall(() -> h.onAfterToolCall(context, state, toolName, result), h.getName(), "onAfterToolCall"));
    }

    public void onBeforeSessionSummarize(AgentContext context, AgentState state) {
        hooks.forEach(h -> safeCall(() -> h.onBeforeSessionSummarize(context, state), h.getName(), "onBeforeSessionSummarize"));
    }

    public void onConversationUpdated(AgentContext context) {
        hooks.forEach(h -> safeCall(() -> h.onConversationUpdated(context), h.getName(), "onConversationUpdated"));
    }

    public void onStoryUpdated(AgentContext context) {
        hooks.forEach(h -> safeCall(() -> h.onStoryUpdated(context), h.getName(), "onStoryUpdated"));
    }

    public void onSessionEnd(AgentContext context, AgentState state) {
        hooks.forEach(h -> safeCall(() -> h.onSessionEnd(context, state), h.getName(), "onSessionEnd"));
    }

    public void onError(AgentContext context, AgentState state, Throwable error) {
        hooks.forEach(h -> safeCall(() -> h.onError(context, state, error), h.getName(), "onError"));
    }

    private void safeCall(Runnable runnable, String hookName, String method) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.warn("Hook {} failed in {}: {}", hookName, method, e.getMessage());
        }
    }
}