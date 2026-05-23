package com.yancy.yharness.memory;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class MemoryService {
    private final MemoryStore memoryStore;

    public MemoryService(MemoryStore memoryStore) {
        this.memoryStore = memoryStore;
    }

    public Story getOrCreateStory(String userId) {
        return memoryStore.getStory(userId)
                .orElseGet(() -> {
                    Story story = new Story();
                    story.setUserId(userId);
                    story.setLeadStage("NEW_LEAD");
                    story.setLanguage("en");
                    story.setTotalConversations(0);
                    story.setVersion(1);
                    memoryStore.saveStory(story);
                    return story;
                });
    }

    public Conversation getOrCreateConversation(String conversationId, String userId, String channelAccountId) {
        return memoryStore.getConversation(conversationId)
                .orElseGet(() -> {
                    Conversation conv = new Conversation();
                    conv.setConversationId(conversationId);
                    conv.setUserId(userId);
                    conv.setChannelAccountId(channelAccountId);
                    conv.setVersion(1);
                    memoryStore.saveConversation(conv);
                    return conv;
                });
    }

    public Session createSession(String conversationId, String userId, String channelAccountId, SessionType type) {
        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setConversationId(conversationId);
        session.setUserId(userId);
        session.setChannelAccountId(channelAccountId);
        session.setSessionType(type);
        return session;
    }

    public void finalizeSession(Session session, String summary) {
        session.setSummary(summary);
        memoryStore.saveSession(session);
    }

    public void updateConversation(Conversation conversation, Session session) {
        Conversation.SessionSummary summary = new Conversation.SessionSummary();
        summary.setSessionId(session.getSessionId());
        summary.setSessionType(session.getSessionType());
        summary.setSummary(session.getSummary());
        summary.setCreatedAt(java.time.LocalDateTime.now());

        conversation.getRecentSessions().add(0, summary);

        if (conversation.getRecentSessions().size() > 5) {
            StringBuilder earlySummary = new StringBuilder();
            for (int i = 5; i < conversation.getRecentSessions().size(); i++) {
                earlySummary.append(conversation.getRecentSessions().get(i).getSummary()).append("\n");
            }
            conversation.setEarlySummary(earlySummary.toString());
            conversation.setRecentSessions(
                    conversation.getRecentSessions().subList(0, 5)
            );
        }

        conversation.setSummary(buildConversationSummary(conversation));
        memoryStore.updateConversationWithVersion(conversation, conversation.getVersion() - 1);
        memoryStore.evictConversationCache(conversation.getConversationId());
    }

    public void updateStoryIfNeeded(Story story, Conversation conversation) {
        story.setLastContactAt(java.time.LocalDateTime.now());
        story.setTotalConversations(story.getTotalConversations() + 1);
        memoryStore.updateStoryWithVersion(story, story.getVersion() - 1);
        memoryStore.evictStoryCache(story.getUserId());
    }

    private String buildConversationSummary(Conversation conversation) {
        StringBuilder sb = new StringBuilder();
        sb.append("[STATE] ").append(conversation.getStateDelta() != null ? conversation.getStateDelta() : "active").append("\n");
        if (conversation.getRecentSessions() != null) {
            for (Conversation.SessionSummary s : conversation.getRecentSessions()) {
                sb.append("[").append(s.getSessionType()).append("] ").append(s.getSummary()).append("\n");
            }
        }
        return sb.toString();
    }
}