
package com.example.yharness.hooks.impl;

import com.example.yharness.context.AgentContext;
import com.example.yharness.core.Action;
import com.example.yharness.core.Thought;
import com.example.yharness.hooks.AgentHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingHook implements AgentHook {

    private static final Logger logger = LoggerFactory.getLogger(LoggingHook.class);

    @Override
    public void onAgentStart(AgentContext context) {
        logger.info("Agent started with context: {}", context.getConversationId());
    }

    @Override
    public void onAgentEnd(AgentContext context) {
        logger.info("Agent ended for context: {}", context.getConversationId());
    }

    @Override
    public void onContextInit(AgentContext context) {
        logger.debug("Context initialized: {}", context.getConversationId());
    }

    @Override
    public void onContextUpdate(AgentContext context) {
        logger.debug("Context updated: {}", context.getConversationId());
    }

    @Override
    public void onMessageReceived(AgentContext context, String message) {
        logger.info("Received message [{}]: {}", context.getConversationId(), message);
    }

    @Override
    public void onMessageSend(AgentContext context, String message) {
        logger.info("Sending message [{}]: {}", context.getConversationId(), message);
    }

    @Override
    public void onToolCall(AgentContext context, Action action) {
        logger.info("Tool call [{}]: {} with args: {}", 
                context.getConversationId(), 
                action.getToolName(), 
                action.getArguments());
    }

    @Override
    public void onToolResult(AgentContext context, String result) {
        logger.info("Tool result [{}]: {}", context.getConversationId(), result);
    }

    @Override
    public void onProviderCall(AgentContext context) {
        logger.debug("Calling provider for context: {}", context.getConversationId());
    }

    @Override
    public void onProviderResponse(AgentContext context, String response) {
        logger.debug("Provider response received for context: {}", context.getConversationId());
    }

    @Override
    public void onError(AgentContext context, Exception exception) {
        logger.error("Error occurred in context {}: {}", context.getConversationId(), exception.getMessage(), exception);
    }

    @Override
    public void onReActStart(AgentContext context) {
        logger.debug("ReAct loop started for context: {}", context.getConversationId());
    }

    @Override
    public void onReActEnd(AgentContext context, long durationMs) {
        logger.info("ReAct loop completed for context {} in {}ms", context.getConversationId(), durationMs);
    }

    @Override
    public void onThoughtGenerated(AgentContext context, Thought thought) {
        logger.debug("Thought generated [{}]: {}", context.getConversationId(), thought.getContent());
    }
}
