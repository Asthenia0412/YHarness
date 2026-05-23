package com.yancy.yharness.scheduler;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Component
public class MockRedisQueue {
    private final ConcurrentSkipListSet<JobEntry> dueQueue = new ConcurrentSkipListSet<>(
            (a, b) -> {
                int cmp = Long.compare(a.dueAt, b.dueAt);
                return cmp != 0 ? cmp : a.jobId.compareTo(b.jobId);
            }
    );
    private final Map<String, String> statusCache = new ConcurrentHashMap<>();
    private final Map<String, String> leaseCache = new ConcurrentHashMap<>();

    public void add(String jobId, long dueAtTimestamp, String status) {
        dueQueue.add(new JobEntry(jobId, dueAtTimestamp));
        statusCache.put(jobId, status);
    }

    public void remove(String jobId) {
        dueQueue.removeIf(e -> e.jobId.equals(jobId));
        statusCache.remove(jobId);
        leaseCache.remove(jobId);
    }

    public java.util.List<String> pollDue(long currentTimestamp, int limit) {
        return dueQueue.stream()
                .filter(e -> e.dueAt <= currentTimestamp)
                .limit(limit)
                .map(e -> e.jobId)
                .collect(Collectors.toList());
    }

    public String getStatus(String jobId) {
        return statusCache.get(jobId);
    }

    public boolean tryAcquireLease(String jobId, String instanceId, int ttlSeconds) {
        return leaseCache.putIfAbsent(jobId, instanceId) == null;
    }

    public void releaseLease(String jobId) {
        leaseCache.remove(jobId);
    }

    public void setStatus(String jobId, String status) {
        statusCache.put(jobId, status);
    }

    public void clear() {
        dueQueue.clear();
        statusCache.clear();
        leaseCache.clear();
    }

    private static class JobEntry {
        final String jobId;
        final long dueAt;

        JobEntry(String jobId, long dueAt) {
            this.jobId = jobId;
            this.dueAt = dueAt;
        }
    }
}