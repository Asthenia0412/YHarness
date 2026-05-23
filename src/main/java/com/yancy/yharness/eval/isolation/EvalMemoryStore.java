package com.yancy.yharness.eval.isolation;

import com.yancy.yharness.memory.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EvalMemoryStore implements MemoryStore {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    private final Map<String, Story> stories = new ConcurrentHashMap<>();
    private final Map<String, String> checkpoints = new ConcurrentHashMap<>();

    @Override
    public void saveSession(Session session) {
        if (!EvalContext.isEvalMode()) return;
        if (session.getCreatedAt() == null) {
            session.setCreatedAt(java.time.LocalDateTime.now());
        }
        sessions.put(session.getSessionId(), session);
    }

    @Override
    public Optional<Session> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<Session> listSessions(String conversationId) {
        return sessions.values().stream()
                .filter(s -> conversationId.equals(s.getConversationId()))
                .sorted(Comparator.comparing(Session::getCreatedAt))
                .toList();
    }

    @Override
    public void saveConversation(Conversation conversation) {
        if (!EvalContext.isEvalMode()) return;
        if (conversation.getCreatedAt() == null) {
            conversation.setCreatedAt(java.time.LocalDateTime.now());
        }
        conversation.setUpdatedAt(java.time.LocalDateTime.now());
        conversations.put(conversation.getConversationId(), conversation);
    }

    @Override
    public Optional<Conversation> getConversation(String conversationId) {
        return Optional.ofNullable(conversations.get(conversationId));
    }

    @Override
    public boolean updateConversationWithVersion(Conversation conversation, int expectedVersion) {
        if (!EvalContext.isEvalMode()) return false;
        Conversation existing = conversations.get(conversation.getConversationId());
        if (existing != null && existing.getVersion() != expectedVersion) return false;
        conversation.setVersion(expectedVersion + 1);
        conversation.setUpdatedAt(java.time.LocalDateTime.now());
        conversations.put(conversation.getConversationId(), conversation);
        return true;
    }

    @Override
    public void saveStory(Story story) {
        if (!EvalContext.isEvalMode()) return;
        if (story.getUpdatedAt() == null) story.setUpdatedAt(java.time.LocalDateTime.now());
        stories.put(story.getUserId(), story);
    }

    @Override
    public Optional<Story> getStory(String userId) {
        return Optional.ofNullable(stories.get(userId));
    }

    @Override
    public boolean updateStoryWithVersion(Story story, int expectedVersion) {
        if (!EvalContext.isEvalMode()) return false;
        Story existing = stories.get(story.getUserId());
        if (existing != null && existing.getVersion() != expectedVersion) return false;
        story.setVersion(expectedVersion + 1);
        story.setUpdatedAt(java.time.LocalDateTime.now());
        stories.put(story.getUserId(), story);
        return true;
    }

    @Override
    public void saveCheckpoint(String conversationId, String checkpointData, int ttlSeconds) {
        if (EvalContext.isEvalMode()) checkpoints.put("cp:" + conversationId, checkpointData);
    }

    @Override
    public Optional<String> getCheckpoint(String conversationId) {
        return Optional.ofNullable(checkpoints.get("cp:" + conversationId));
    }

    @Override
    public void deleteCheckpoint(String conversationId) {
        checkpoints.remove("cp:" + conversationId);
    }

    @Override
    public void cacheConversation(String conversationId, Conversation conversation, int ttlSeconds) {}
    @Override
    public void cacheStory(String userId, Story story, int ttlSeconds) {}
    @Override
    public void evictConversationCache(String conversationId) {}
    @Override
    public void evictStoryCache(String userId) {}

    public void clearAll() {
        sessions.clear();
        conversations.clear();
        stories.clear();
        checkpoints.clear();
    }
}