package com.yancy.yharness.memory;

import java.util.List;
import java.util.Optional;

public interface MemoryStore {
    void saveSession(Session session);
    Optional<Session> getSession(String sessionId);
    List<Session> listSessions(String conversationId);

    void saveConversation(Conversation conversation);
    Optional<Conversation> getConversation(String conversationId);
    boolean updateConversationWithVersion(Conversation conversation, int expectedVersion);

    void saveStory(Story story);
    Optional<Story> getStory(String userId);
    boolean updateStoryWithVersion(Story story, int expectedVersion);

    void saveCheckpoint(String conversationId, String checkpointData, int ttlSeconds);
    Optional<String> getCheckpoint(String conversationId);
    void deleteCheckpoint(String conversationId);

    void cacheConversation(String conversationId, Conversation conversation, int ttlSeconds);
    void cacheStory(String userId, Story story, int ttlSeconds);
    void evictConversationCache(String conversationId);
    void evictStoryCache(String userId);
}