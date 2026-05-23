package com.yancy.yharness.pipeline;

import com.yancy.yharness.model.TaskRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockTaskRepository {
    private static final Logger log = LoggerFactory.getLogger(MockTaskRepository.class);
    private final ConcurrentHashMap<String, TaskRecord> store = new ConcurrentHashMap<>();

    public TaskRecord create(String userId, String conversationId, String taskType, String userMessage) {
        TaskRecord record = new TaskRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setConversationId(conversationId);
        record.setTaskType(taskType);
        record.setUserMessage(userMessage);
        record.setStatus(TaskRecord.STATUS_PENDING);
        record.setCreatedAt(LocalDateTime.now());
        store.put(record.getId(), record);
        log.debug("[TaskRepo] Created task: id={}, userId={}, type={}", record.getId(), userId, taskType);
        return record;
    }

    public void update(TaskRecord record) {
        store.put(record.getId(), record);
    }

    public TaskRecord get(String taskId) {
        return store.get(taskId);
    }
}