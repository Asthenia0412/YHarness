package com.yancy.yharness.scheduler;

import com.yancy.yharness.model.AgentResponse;
import com.yancy.yharness.pipeline.DispatchPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class Engine {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);
    private final Planner planner;
    private final MockRedisQueue redisQueue;
    private final DispatchPipeline dispatchPipeline;

    public Engine(Planner planner, MockRedisQueue redisQueue,
                  DispatchPipeline dispatchPipeline) {
        this.planner = planner;
        this.redisQueue = redisQueue;
        this.dispatchPipeline = dispatchPipeline;
    }

    public void process(Long jobId) {
        ClientTaskJob job = planner.getJob(jobId);
        if (job == null) {
            log.warn("Job {} not found", jobId);
            redisQueue.remove(String.valueOf(jobId));
            return;
        }

        if (!"PENDING".equals(job.getStatus())) {
            redisQueue.setStatus(String.valueOf(jobId), job.getStatus());
            redisQueue.remove(String.valueOf(jobId));
            return;
        }

        try {
            job.setStatus("PROCESSING");
            job.setUpdatedAt(LocalDateTime.now());

            AgentResponse response = dispatchPipeline.dispatchJob(job);

            job.setStatus("DONE");
            job.setUpdatedAt(LocalDateTime.now());

            redisQueue.setStatus(String.valueOf(jobId), "DONE");
            redisQueue.releaseLease(String.valueOf(jobId));
            redisQueue.remove(String.valueOf(jobId));

            log.info("Job {} completed successfully: replyLength={}", jobId,
                    response.getFinalReply() != null ? response.getFinalReply().length() : 0);
        } catch (Exception e) {
            log.error("Job {} failed: {}", jobId, e.getMessage());
            job.setStatus("FAILED");
            job.setUpdatedAt(LocalDateTime.now());
            redisQueue.setStatus(String.valueOf(jobId), "FAILED");
            redisQueue.releaseLease(String.valueOf(jobId));
            redisQueue.remove(String.valueOf(jobId));
        }
    }
}