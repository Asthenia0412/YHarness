package com.yancy.yharness.memory;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class MockMemoryStore implements MemoryStore {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    private final Map<String, Story> stories = new ConcurrentHashMap<>();
    private final Map<String, String> checkpoints = new ConcurrentHashMap<>();
    private final Map<String, Conversation> conversationCache = new ConcurrentHashMap<>();
    private final Map<String, Story> storyCache = new ConcurrentHashMap<>();

    @Override
    public void saveSession(Session session) {
        if (session.getCreatedAt() == null) {
            session.setCreatedAt(LocalDateTime.now());
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
                .collect(Collectors.toList());
    }

    @Override
    public void saveConversation(Conversation conversation) {
        if (conversation.getCreatedAt() == null) {
            conversation.setCreatedAt(LocalDateTime.now());
        }
        conversation.setUpdatedAt(LocalDateTime.now());
        conversations.put(conversation.getConversationId(), conversation);
    }

    @Override
    public Optional<Conversation> getConversation(String conversationId) {
        return Optional.ofNullable(conversations.get(conversationId));
    }

    @Override
    public boolean updateConversationWithVersion(Conversation conversation, int expectedVersion) {
        Conversation existing = conversations.get(conversation.getConversationId());
        if (existing != null && existing.getVersion() != expectedVersion) {
            return false;
        }
        conversation.setVersion(expectedVersion + 1);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversations.put(conversation.getConversationId(), conversation);
        return true;
    }

    @Override
    public void saveStory(Story story) {
        if (story.getUpdatedAt() == null) {
            story.setUpdatedAt(LocalDateTime.now());
        }
        stories.put(story.getUserId(), story);
    }

    @Override
    public Optional<Story> getStory(String userId) {
        return Optional.ofNullable(stories.get(userId));
    }

    @Override
    public boolean updateStoryWithVersion(Story story, int expectedVersion) {
        Story existing = stories.get(story.getUserId());
        if (existing != null && existing.getVersion() != expectedVersion) {
            return false;
        }
        story.setVersion(expectedVersion + 1);
        story.setUpdatedAt(LocalDateTime.now());
        stories.put(story.getUserId(), story);
        return true;
    }

    @Override
    public void saveCheckpoint(String conversationId, String checkpointData, int ttlSeconds) {
        checkpoints.put("cp:" + conversationId, checkpointData);
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
    public void cacheConversation(String conversationId, Conversation conversation, int ttlSeconds) {
        conversationCache.put("conv:" + conversationId, conversation);
    }

    @Override
    public void cacheStory(String userId, Story story, int ttlSeconds) {
        storyCache.put("story:" + userId, story);
    }

    @Override
    public void evictConversationCache(String conversationId) {
        conversationCache.remove("conv:" + conversationId);
    }

    @Override
    public void evictStoryCache(String userId) {
        storyCache.remove("story:" + userId);
    }

    public Optional<Conversation> getCachedConversation(String conversationId) {
        return Optional.ofNullable(conversationCache.get("conv:" + conversationId));
    }

    public Optional<Story> getCachedStory(String userId) {
        return Optional.ofNullable(storyCache.get("story:" + userId));
    }
}