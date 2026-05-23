package com.yancy.yharness.pipeline;

import com.yancy.yharness.core.Agent;
import com.yancy.yharness.eval.isolation.EvalContext;
import com.yancy.yharness.model.AgentRequest;
import com.yancy.yharness.model.AgentResponse;
import com.yancy.yharness.model.TaskRecord;
import com.yancy.yharness.model.TaskType;
import com.yancy.yharness.preparer.RequestPreparer;
import com.yancy.yharness.scheduler.ClientTaskJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class DispatchPipeline {
    private static final Logger log = LoggerFactory.getLogger(DispatchPipeline.class);

    private final RequestPreparer requestPreparer;
    private final Agent agent;
    private final MockTaskRepository taskRepository;
    private final Executor agentExecutor;
    private final ConcurrentHashMap<String, CompletableFuture<AgentResponse>> pendingFutures;

    private static final long CHAT_TIMEOUT_MS = 10000;

    public DispatchPipeline(RequestPreparer requestPreparer, Agent agent,
                            MockTaskRepository taskRepository, Executor agentExecutor) {
        this.requestPreparer = requestPreparer;
        this.agent = agent;
        this.taskRepository = taskRepository;
        this.agentExecutor = agentExecutor;
        this.pendingFutures = new ConcurrentHashMap<>();
    }

    public DispatchResult dispatchChat(String userId, String message, String conversationId) {
        return dispatchChat(userId, message, conversationId, null);
    }

    public DispatchResult dispatchChat(String userId, String message, String conversationId,
                                        Map<String, Object> metadata) {
        AgentRequest request = requestPreparer.prepareFromMessage(userId, message, conversationId);
        if (metadata != null) {
            if (request.getMetadata() == null) {
                request.setMetadata(new HashMap<>());
            }
            request.getMetadata().putAll(metadata);
        }

        TaskRecord record = taskRepository.create(userId, conversationId, "INBOUND", message);
        record.markRunning();
        taskRepository.update(record);

        CompletableFuture<AgentResponse> future = CompletableFuture.supplyAsync(() -> {
            AgentRequest prepared = fillDefaults(request);
            AgentResponse response = agent.handle(prepared);
            record.markDone(
                    response.getFinalReply(),
                    response.getElapsedMs(),
                    response.getToolCalls() != null ? response.getToolCalls().size() : 0,
                    response.getTokenUsage() != null ? response.getTokenUsage().getTotalTokens() : 0
            );
            taskRepository.update(record);
            return response;
        }, agentExecutor);

        pendingFutures.put(record.getId(), future);

        try {
            AgentResponse response = future.get(CHAT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            pendingFutures.remove(record.getId());
            return DispatchResult.done(record.getId(), response);
        } catch (TimeoutException e) {
            log.warn("[DISPATCH] Chat timeout for task {} after {}ms, returning taskId for polling",
                    record.getId(), CHAT_TIMEOUT_MS);
            return DispatchResult.pending(record.getId());
        } catch (ExecutionException e) {
            pendingFutures.remove(record.getId());
            String errMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            record.markFailed(errMsg);
            taskRepository.update(record);
            log.error("[DISPATCH] Chat failed for task {}: {}", record.getId(), errMsg);
            return DispatchResult.failed(record.getId(), errMsg);
        } catch (CancellationException e) {
            pendingFutures.remove(record.getId());
            record.markFailed("Cancelled");
            taskRepository.update(record);
            return DispatchResult.failed(record.getId(), "Task cancelled");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pendingFutures.remove(record.getId());
            record.markFailed("Interrupted");
            taskRepository.update(record);
            return DispatchResult.failed(record.getId(), "Interrupted");
        }
    }

    public AgentResponse dispatchJob(ClientTaskJob job) {
        AgentRequest request = requestPreparer.prepare(job);
        AgentRequest prepared = fillDefaults(request);

        TaskRecord record = taskRepository.create(
                job.getUserId(),
                request.getConversationId(),
                job.getTaskName() != null ? job.getTaskName() : "JOB",
                request.getUserMessage()
        );
        record.markRunning();
        taskRepository.update(record);

        try {
            AgentResponse response = agent.handle(prepared);
            record.markDone(
                    response.getFinalReply(),
                    response.getElapsedMs(),
                    response.getToolCalls() != null ? response.getToolCalls().size() : 0,
                    response.getTokenUsage() != null ? response.getTokenUsage().getTotalTokens() : 0
            );
            taskRepository.update(record);
            return response;
        } catch (Exception e) {
            record.markFailed(e.getMessage());
            taskRepository.update(record);
            throw e;
        }
    }

    public AgentResponse dispatchEval(String userId, String message, String conversationId) {
        AgentRequest request = requestPreparer.prepareFromMessage(userId, message, conversationId);

        TaskRecord record = taskRepository.create(userId, conversationId, "EVAL", message);

        EvalContext.enter(userId);
        record.markRunning();
        taskRepository.update(record);

        try {
            AgentRequest prepared = fillDefaults(request);
            if (prepared.getMetadata() == null) {
                prepared.setMetadata(new HashMap<>());
            }
            prepared.getMetadata().put("eval_mode", "true");

            AgentResponse response = agent.handle(prepared);
            record.markDone(
                    response.getFinalReply(),
                    response.getElapsedMs(),
                    response.getToolCalls() != null ? response.getToolCalls().size() : 0,
                    response.getTokenUsage() != null ? response.getTokenUsage().getTotalTokens() : 0
            );
            taskRepository.update(record);
            return response;
        } catch (Exception e) {
            record.markFailed(e.getMessage());
            taskRepository.update(record);
            throw e;
        } finally {
            EvalContext.exit();
        }
    }

    public AgentResponse dispatchDirect(AgentRequest request) {
        TaskRecord record = taskRepository.create(
                request.getUserId(),
                request.getConversationId(),
                request.getTaskType() != null ? request.getTaskType().name() : "DIRECT",
                request.getUserMessage()
        );
        record.markRunning();
        taskRepository.update(record);

        try {
            AgentRequest prepared = fillDefaults(request);
            AgentResponse response = agent.handle(prepared);
            record.markDone(
                    response.getFinalReply(),
                    response.getElapsedMs(),
                    response.getToolCalls() != null ? response.getToolCalls().size() : 0,
                    response.getTokenUsage() != null ? response.getTokenUsage().getTotalTokens() : 0
            );
            taskRepository.update(record);
            return response;
        } catch (Exception e) {
            record.markFailed(e.getMessage());
            taskRepository.update(record);
            throw e;
        }
    }

    public TaskRecord getTaskResult(String taskId) {
        TaskRecord record = taskRepository.get(taskId);
        if (record == null) {
            return null;
        }
        if (TaskRecord.STATUS_RUNNING.equals(record.getStatus())) {
            CompletableFuture<AgentResponse> future = pendingFutures.get(taskId);
            if (future != null && future.isDone()) {
                try {
                    future.get(0, TimeUnit.MILLISECONDS);
                } catch (Exception ignored) {
                }
                TaskRecord updated = taskRepository.get(taskId);
                if (updated != null) {
                    return updated;
                }
            }
        }
        return record;
    }

    private AgentRequest fillDefaults(AgentRequest request) {
        if (request.getTaskType() == null) {
            request.setTaskType(TaskType.INBOUND);
        }
        if (request.getLanguageCode() == null) {
            request.setLanguageCode("en");
        }
        if (request.getChannelId() == null) {
            request.setChannelId("api");
        }
        if (request.getChannelAccountId() == null) {
            request.setChannelAccountId("default");
        }
        if (request.getTimezone() == null) {
            request.setTimezone("Asia/Bangkok");
        }
        return request;
    }
}