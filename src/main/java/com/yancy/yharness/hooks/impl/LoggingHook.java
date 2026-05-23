package com.yancy.yharness.hooks.impl;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.core.AgentState;
import com.yancy.yharness.hooks.AgentHook;
import com.yancy.yharness.hooks.HookType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingHook implements AgentHook {
    private static final Logger log = LoggerFactory.getLogger(LoggingHook.class);

    @Override
    public String getName() { return "LoggingHook"; }

    @Override
    public HookType getType() { return HookType.SESSION_START; }

    @Override
    public void onSessionStart(AgentContext context, AgentState state) {
        log.info("[SessionStart] userId={}, conversationId={}, taskType={}",
                context.getUserId(), context.getConversationId(),
                state.getInputState().getTaskType());
    }

    @Override
    public void onContextAssembling(AgentContext context) {
        log.debug("[ContextAssembling] assembling context for conversation={}", context.getConversationId());
    }

    @Override
    public void onMemoryRetrieved(AgentContext context) {
        log.debug("[MemoryRetrieved] story loaded: {}, conversation loaded: {}",
                context.getStory() != null, context.getConversation() != null);
    }

    @Override
    public void onBeforeModelCall(AgentContext context, AgentState state) {
        log.debug("[BeforeModelCall] preparing model request");
    }

    @Override
    public void onAfterModelCall(AgentContext context, AgentState state, String modelResponse) {
        log.debug("[AfterModelCall] received response of length {}", 
                modelResponse != null ? modelResponse.length() : 0);
    }

    @Override
    public void onBeforeToolCall(AgentContext context, AgentState state, String toolName) {
        log.info("[BeforeToolCall] invoking tool: {}", toolName);
    }

    @Override
    public void onAfterToolCall(AgentContext context, AgentState state, String toolName, String result) {
        log.info("[AfterToolCall] tool: {}, result length: {}", toolName, 
                result != null ? result.length() : 0);
    }

    @Override
    public void onBeforeSessionSummarize(AgentContext context, AgentState state) {
        log.debug("[BeforeSessionSummarize] preparing session summary");
    }

    @Override
    public void onConversationUpdated(AgentContext context) {
        log.info("[ConversationUpdated] conversation={} updated", context.getConversationId());
    }

    @Override
    public void onStoryUpdated(AgentContext context) {
        log.info("[StoryUpdated] story for user={} updated", context.getUserId());
    }

    @Override
    public void onSessionEnd(AgentContext context, AgentState state) {
        log.info("[SessionEnd] session={} ended, elapsed={}ms",
                context.getSessionId(), state.getPerfState().getElapsedMs());
    }

    @Override
    public void onError(AgentContext context, AgentState state, Throwable error) {
        log.error("[Error] session={} error: {}", context.getSessionId(), error.getMessage());
    }
}