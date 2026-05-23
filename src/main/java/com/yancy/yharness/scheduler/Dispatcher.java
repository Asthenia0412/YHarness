package com.yancy.yharness.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);
    private final MockRedisQueue redisQueue;
    private final Engine engine;
    private final String instanceId;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Dispatcher(MockRedisQueue redisQueue, Engine engine) {
        this.redisQueue = redisQueue;
        this.engine = engine;
        this.instanceId = "instance-" + System.currentTimeMillis();
    }

    public void start(int pollIntervalMs) {
        scheduler.scheduleWithFixedDelay(this::dispatchCycle, 0, pollIntervalMs, TimeUnit.MILLISECONDS);
        log.info("Dispatcher started on {} with interval {}ms", instanceId, pollIntervalMs);
    }

    public void stop() {
        scheduler.shutdown();
        log.info("Dispatcher stopped");
    }

    private void dispatchCycle() {
        try {
            List<String> dueJobs = redisQueue.pollDue(System.currentTimeMillis(), 100);
            for (String jobId : dueJobs) {
                String status = redisQueue.getStatus(jobId);
                if (status == null || "CANCELLED".equals(status) || "DONE".equals(status) || "FAILED".equals(status)) {
                    redisQueue.remove(jobId);
                    continue;
                }
                if ("PENDING".equals(status)) {
                    boolean leased = redisQueue.tryAcquireLease(jobId, instanceId, 30);
                    if (leased) {
                        log.info("Dispatcher leased job {} to engine", jobId);
                        engine.process(Long.parseLong(jobId));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Dispatch cycle error: {}", e.getMessage());
        }
    }
}