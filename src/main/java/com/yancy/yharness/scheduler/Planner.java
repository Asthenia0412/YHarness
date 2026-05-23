package com.yancy.yharness.scheduler;

import com.yancy.yharness.model.TaskType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class Planner {
    private final MockRedisQueue redisQueue;
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<String, ClientTaskJob> jobStore = new ConcurrentHashMap<>();

    public Planner(MockRedisQueue redisQueue) {
        this.redisQueue = redisQueue;
    }

    public ClientTaskJob planInbound(String userId, String channelAccountId, String accountId) {
        cancelOtherJobs(userId);

        ClientTaskJob job = createJob(
                "INBOUND_" + userId + "_" + System.currentTimeMillis(),
                "INBOUND_MESSAGE",
                userId, channelAccountId, accountId,
                "PENDING", LocalDateTime.now()
        );

        job.setStrategySummary("{\"stage\":\"inbound_response\",\"goal\":\"answer_merchant_inquiry\"}");
        return job;
    }

    public ClientTaskJob planOutreach(String userId, String channelAccountId, String accountId,
                                       String strategySummary, LocalDateTime dueAt) {
        String taskKey = "OUTREACH_" + userId + "_" + dueAt.toString();
        if (jobStore.values().stream().anyMatch(j ->
                taskKey.equals(j.getTaskKey()) && !"DONE".equals(j.getStatus()) && !"CANCELLED".equals(j.getStatus()))) {
            return null;
        }

        ClientTaskJob job = createJob(
                taskKey, "OUTREACH_REPORT_PREP",
                userId, channelAccountId, accountId,
                "PENDING", dueAt
        );
        job.setStrategySummary(strategySummary);
        return job;
    }

    public ClientTaskJob planPostInbound(String userId, String channelAccountId) {
        ClientTaskJob job = createJob(
                "POST_INBOUND_" + userId + "_" + System.currentTimeMillis(),
                "TASK_TRACING",
                userId, channelAccountId, null,
                "PENDING", LocalDateTime.now().plusMinutes(30)
        );
        job.setStrategySummary("{\"stage\":\"follow_up\",\"goal\":\"check_if_need_follow_up\"}");
        return job;
    }

    public void cancelOtherJobs(String userId) {
        jobStore.values().stream()
                .filter(j -> userId.equals(j.getUserId())
                        && ("PENDING".equals(j.getStatus()) || "LEASED".equals(j.getStatus())))
                .forEach(j -> {
                    j.setStatus("CANCELLED");
                    j.setUpdatedAt(LocalDateTime.now());
                    redisQueue.remove(String.valueOf(j.getId()));
                });
    }

    public ClientTaskJob getJob(Long jobId) {
        return jobStore.get(jobId);
    }

    private ClientTaskJob createJob(String taskKey, String taskName, String userId,
                                     String channelAccountId, String accountId,
                                     String status, LocalDateTime dueAt) {
        ClientTaskJob job = new ClientTaskJob();
        Long id = idGenerator.incrementAndGet();
        job.setId(id);
        job.setTaskKey(taskKey);
        job.setTaskName(taskName);
        job.setUserId(userId);
        job.setChannelAccountId(channelAccountId);
        job.setAccountId(accountId);
        job.setStatus(status);
        job.setDueAt(dueAt);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        jobStore.put(String.valueOf(id), job);

        long dueAtTimestamp = dueAt != null
                ? dueAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : System.currentTimeMillis();
        redisQueue.add(String.valueOf(id), dueAtTimestamp, status);
        return job;
    }
}