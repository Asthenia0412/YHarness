
package com.yancy.yharness.hooks.impl;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.core.Action;
import com.yancy.yharness.core.Thought;
import com.yancy.yharness.hooks.AgentHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetricsHook implements AgentHook {

    private static final Logger logger = LoggerFactory.getLogger(MetricsHook.class);
    
    private final Map<String, Long> reactStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> messageCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> toolCallCounts = new ConcurrentHashMap<>();
    private long totalMessages = 0;
    private long totalToolCalls = 0;
    private long totalReActLoops = 0;
    private long totalReActDuration = 0;

    @Override
    public void onAgentStart(AgentContext context) {
        messageCounts.put(context.getConversationId(), 0);
        toolCallCounts.put(context.getConversationId(), 0);
    }

    @Override
    public void onAgentEnd(AgentContext context) {
        messageCounts.remove(context.getConversationId());
        toolCallCounts.remove(context.getConversationId());
        reactStartTimes.remove(context.getConversationId());
    }

    @Override
    public void onContextInit(AgentContext context) {
    }

    @Override
    public void onContextUpdate(AgentContext context) {
    }

    @Override
    public void onMessageReceived(AgentContext context, String message) {
        messageCounts.merge(context.getConversationId(), 1, Integer::sum);
        totalMessages++;
    }

    @Override
    public void onMessageSend(AgentContext context, String message) {
    }

    @Override
    public void onToolCall(AgentContext context, Action action) {
        toolCallCounts.merge(context.getConversationId(), 1, Integer::sum);
        totalToolCalls++;
    }

    @Override
    public void onToolResult(AgentContext context, String result) {
    }

    @Override
    public void onProviderCall(AgentContext context) {
    }

    @Override
    public void onProviderResponse(AgentContext context, String response) {
    }

    @Override
    public void onError(AgentContext context, Exception exception) {
    }

    @Override
    public void onReActStart(AgentContext context) {
        reactStartTimes.put(context.getConversationId(), System.currentTimeMillis());
    }

    @Override
    public void onReActEnd(AgentContext context, long durationMs) {
        reactStartTimes.remove(context.getConversationId());
        totalReActLoops++;
        totalReActDuration += durationMs;
    }

    @Override
    public void onThoughtGenerated(AgentContext context, Thought thought) {
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalMessages", totalMessages);
        metrics.put("totalToolCalls", totalToolCalls);
        metrics.put("totalReActLoops", totalReActLoops);
        metrics.put("averageReActDuration", totalReActLoops > 0 ? totalReActDuration / totalReActLoops : 0);
        metrics.put("activeConversations", messageCounts.size());
        return metrics;
    }

    public void logMetrics() {
        logger.info("=== Agent Metrics ===");
        logger.info("Total Messages: {}", totalMessages);
        logger.info("Total Tool Calls: {}", totalToolCalls);
        logger.info("Total ReAct Loops: {}", totalReActLoops);
        logger.info("Average ReAct Duration: {}ms", totalReActLoops > 0 ? totalReActDuration / totalReActLoops : 0);
        logger.info("Active Conversations: {}", messageCounts.size());
        logger.info("====================");
    }
}
