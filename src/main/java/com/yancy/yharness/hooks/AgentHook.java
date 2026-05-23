package com.yancy.yharness.hooks;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.core.AgentState;

public interface AgentHook {
    String getName();
    HookType getType();
    void onSessionStart(AgentContext context, AgentState state);
    void onContextAssembling(AgentContext context);
    void onMemoryRetrieved(AgentContext context);
    void onBeforeModelCall(AgentContext context, AgentState state);
    void onAfterModelCall(AgentContext context, AgentState state, String modelResponse);
    void onBeforeToolCall(AgentContext context, AgentState state, String toolName);
    void onAfterToolCall(AgentContext context, AgentState state, String toolName, String result);
    void onBeforeSessionSummarize(AgentContext context, AgentState state);
    void onConversationUpdated(AgentContext context);
    void onStoryUpdated(AgentContext context);
    void onSessionEnd(AgentContext context, AgentState state);
    void onError(AgentContext context, AgentState state, Throwable error);
}