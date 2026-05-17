
package com.yancy.yharness.hooks;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.core.Action;
import com.yancy.yharness.core.Thought;

public interface AgentHook {
    
    void onAgentStart(AgentContext context);
    
    void onAgentEnd(AgentContext context);
    
    void onContextInit(AgentContext context);
    
    void onContextUpdate(AgentContext context);
    
    void onMessageReceived(AgentContext context, String message);
    
    void onMessageSend(AgentContext context, String message);
    
    void onToolCall(AgentContext context, Action action);
    
    void onToolResult(AgentContext context, String result);
    
    void onProviderCall(AgentContext context);
    
    void onProviderResponse(AgentContext context, String response);
    
    void onError(AgentContext context, Exception exception);
    
    void onReActStart(AgentContext context);
    
    void onReActEnd(AgentContext context, long durationMs);
    
    void onThoughtGenerated(AgentContext context, Thought thought);
}
