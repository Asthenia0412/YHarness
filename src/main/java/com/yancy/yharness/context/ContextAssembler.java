package com.yancy.yharness.context;

import com.yancy.yharness.memory.Conversation;
import com.yancy.yharness.memory.MemoryService;
import com.yancy.yharness.memory.SessionType;
import com.yancy.yharness.memory.Session;
import com.yancy.yharness.memory.Story;
import com.yancy.yharness.core.AgentState;
import com.yancy.yharness.model.AgentRequest;
import com.yancy.yharness.tools.ToolDefinition;
import com.yancy.yharness.tools.registry.ToolVisibilityResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ContextAssembler {
    private final MemoryService memoryService;
    private final ToolVisibilityResolver visibilityResolver;

    public ContextAssembler(MemoryService memoryService, ToolVisibilityResolver visibilityResolver) {
        this.memoryService = memoryService;
        this.visibilityResolver = visibilityResolver;
    }

    public AssemblyResult assemble(AgentRequest request) {
        String conversationId = request.getConversationId() != null
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        Story story = memoryService.getOrCreateStory(request.getUserId());
        Conversation conversation = memoryService.getOrCreateConversation(
                conversationId, request.getUserId(), request.getChannelAccountId());

        AgentContext context = new AgentContext();
        context.setUserId(request.getUserId());
        context.setConversationId(conversationId);
        context.setSessionId(UUID.randomUUID().toString());
        context.setUserMessage(request.getUserMessage());
        context.setStory(story);
        context.setConversation(conversation);
        context.setSystemPrompt(buildSystemPrompt(request));
        context.setOutputPolicy(buildOutputPolicy(request));

        List<ToolDefinition> visibleTools = visibilityResolver.resolveVisibleTools(context);
        context.setVisibleTools(visibleTools);

        AssemblyResult result = new AssemblyResult();
        result.setContext(context);
        result.setSessionId(context.getSessionId());
        result.setConversationId(conversationId);
        return result;
    }

    public void finalizeSession(AgentState state, AssemblyResult assembly, AgentRequest request) {
        Session session = memoryService.createSession(
                assembly.getConversationId(),
                request.getUserId(),
                request.getChannelAccountId(),
                request.getTaskType() == com.yancy.yharness.model.TaskType.INBOUND
                        ? SessionType.INBOUND : SessionType.OUTREACH
        );
        session.setInputMessage(request.getUserMessage());
        session.setFinalReply(state.getOutputState().getFinalAnswer());
        session.setTokenUsage(state.getPerfState().getTokenUsage().getTotalTokens());
        session.setElapsedMs(state.getPerfState().getElapsedMs());

        String summary = buildSessionSummary(state, request);
        memoryService.finalizeSession(session, summary);

        Conversation conversation = assembly.getContext().getConversation();
        memoryService.updateConversation(conversation, session);

        Story story = assembly.getContext().getStory();
        memoryService.updateStoryIfNeeded(story, conversation);
    }

    private String buildSystemPrompt(AgentRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a professional AI sales assistant for an advertising platform. ");
        sb.append("Your role is to help merchants understand their advertising performance, ");
        sb.append("provide recommendations, and answer questions about promotions and campaigns.\n\n");
        sb.append("You can ONLY query information using the available tools. ");
        sb.append("You must NEVER execute any write operations.\n");
        sb.append("Always respond in a helpful, professional manner.\n");
        sb.append("Language: ").append(request.getLanguageCode()).append("\n");
        sb.append("Channel: ").append(request.getChannelId()).append("\n");

        if (request.getTaskType() == com.yancy.yharness.model.TaskType.OUTREACH) {
            sb.append("\nThis is an OUTREACH session. The merchant has not sent a message yet. ");
            sb.append("You need to initiate the conversation professionally.");
        }

        return sb.toString();
    }

    private String buildOutputPolicy(AgentRequest request) {
        return "Allowed to ask follow-up questions. Prefer using tools before answering. "
                + "If information is insufficient, ask for clarification.";
    }

    private String buildSessionSummary(AgentState state, AgentRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("[INTENT] ").append(request.getUserMessage() != null ? request.getUserMessage() : "OUTREACH").append("\n");
        sb.append("[REPLY] ").append(state.getOutputState().getFinalAnswer()).append("\n");
        if (state.getPerfState().getTokenUsage() != null) {
            sb.append("[TOKENS] ").append(state.getPerfState().getTokenUsage().getTotalTokens()).append("\n");
        }
        return sb.toString();
    }

    public static class AssemblyResult {
        private AgentContext context;
        private String sessionId;
        private String conversationId;

        public AgentContext getContext() { return context; }
        public void setContext(AgentContext context) { this.context = context; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    }
}