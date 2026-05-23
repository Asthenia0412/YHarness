package com.yancy.yharness.eval.service;

import com.yancy.yharness.eval.isolation.EvalContext;
import com.yancy.yharness.eval.isolation.EvalGuard;
import com.yancy.yharness.eval.metrics.EvalMetrics;
import com.yancy.yharness.eval.model.*;
import com.yancy.yharness.eval.store.MockEvalStore;
import com.yancy.yharness.eval.grader.CompositeGrader;
import com.yancy.yharness.pipeline.DispatchPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EvalService {
    private static final Logger log = LoggerFactory.getLogger(EvalService.class);
    private final MockEvalStore store;
    private final CompositeGrader compositeGrader;
    private final EvalGuard evalGuard;
    private final DispatchPipeline dispatchPipeline;

    public EvalService(MockEvalStore store, CompositeGrader compositeGrader,
                       EvalGuard evalGuard, DispatchPipeline dispatchPipeline) {
        this.store = store;
        this.compositeGrader = compositeGrader;
        this.evalGuard = evalGuard;
        this.dispatchPipeline = dispatchPipeline;
    }

    public EvalMetrics runSuite(EvalSuite suite) {
        List<EvalTask> tasks = store.getTasksBySuite(suite.getId());
        log.info("[EvalService] Running suite '{}' with {} tasks", suite.getName(), tasks.size());

        List<EvalMetrics.EvalTrialSummary> summaries = new ArrayList<>();
        for (EvalTask task : tasks) {
            List<EvalTrial> trials = runTask(task.getId(), suite.getMaxTrialsPerTask());
            for (EvalTrial trial : trials) {
                EvalMetrics.EvalTrialSummary summary = new EvalMetrics.EvalTrialSummary();
                summary.setTaskId(task.getId());
                summary.setPassed(trial.isPassed());
                summary.setScore(trial.getGraderResults() != null
                        ? trial.getGraderResults().values().stream()
                        .mapToDouble(GraderResult::getScore).average().orElse(0) : 0);
                summary.setElapsedMs(trial.getElapsedMs());
                summary.setTokenUsage(trial.getTokenUsage());
                summary.setToolCallCount(trial.getToolCallCount());
                summary.setFailureReason(trial.getFailureReason());
                summaries.add(summary);
            }
        }

        EvalMetrics metrics = EvalMetrics.aggregate(summaries);
        log.info("[EvalService] Suite '{}' complete: passRate={}, avgScore={}",
                suite.getName(), metrics.getPassRate(), metrics.getAvgScore());
        return metrics;
    }

    public List<EvalTrial> runTask(String taskId, int numTrials) {
        EvalTask task = store.getTask(taskId);
        if (task == null) {
            log.warn("[EvalService] Task not found: {}", taskId);
            return List.of();
        }

        log.info("[EvalService] Running task '{}' with {} trials", task.getName(), numTrials);

        List<EvalTrial> trialResults = new ArrayList<>();
        for (int i = 0; i < numTrials; i++) {
            EvalTrial trial = executeSingleTrial(task, i + 1);
            store.saveTrial(trial);
            trialResults.add(trial);
        }

        return trialResults;
    }

    private EvalTrial executeSingleTrial(EvalTask task, int attemptNumber) {
        long startTime = System.currentTimeMillis();
        EvalTrial trial = new EvalTrial();
        trial.setId(UUID.randomUUID().toString());
        trial.setTaskId(task.getId());
        trial.setAttemptNumber(attemptNumber);
        trial.setExecutedAt(LocalDateTime.now());

        EvalTranscript transcript = new EvalTranscript();
        transcript.setTrialId(trial.getId());

        String evalUserId = "eval_user_" + task.getId();
        String evalConvId = "eval_conv_" + task.getId();
        String userMessage = task.getDescription() != null ? task.getDescription() : task.getName();

        try {
            com.yancy.yharness.model.AgentResponse response = dispatchPipeline.dispatchEval(
                    evalUserId, userMessage, evalConvId
            );

            transcript.setSystemPrompt(userMessage);
            List<EvalTranscript.TranscriptEntry> entries = new ArrayList<>();
            EvalTranscript.TranscriptEntry entry = new EvalTranscript.TranscriptEntry();
            entry.setRole("assistant");
            entry.setContent(response.getFinalReply());
            entries.add(entry);

            if (response.getToolCalls() != null) {
                for (com.yancy.yharness.model.ToolCallRecord tc : response.getToolCalls()) {
                    EvalTranscript.TranscriptEntry toolEntry = new EvalTranscript.TranscriptEntry();
                    toolEntry.setRole("tool");
                    toolEntry.setToolName(tc.getToolName());
                    toolEntry.setToolResult(tc.getResult());
                    entries.add(toolEntry);
                }
            }
            transcript.setEntries(entries);
            trial.setTranscript(transcript);
            trial.setElapsedMs(System.currentTimeMillis() - startTime);
            trial.setToolCallCount(response.getToolCalls() != null ? response.getToolCalls().size() : 0);
            trial.setTokenUsage(response.getTokenUsage() != null ? response.getTokenUsage().getTotalTokens() : 0);
            trial.setOutcome(evalGuard.captureOutcome(transcript));

            Map<String, GraderResult> graderResults = new HashMap<>();
            GraderResult result = compositeGrader.grade(task, transcript);
            graderResults.put(compositeGrader.name(), result);
            trial.setGraderResults(graderResults);
            trial.setPassed(result.isPassed());
            if (!result.isPassed()) {
                trial.setFailureReason(result.getDetails());
            }

        } catch (Exception e) {
            log.warn("[EvalService] Trial {} failed: {}", trial.getId(), e.getMessage());
            trial.setPassed(false);
            trial.setFailureReason("Execution error: " + e.getMessage());
            trial.setElapsedMs(System.currentTimeMillis() - startTime);
        }

        return trial;
    }

    public void registerTask(EvalTask task) {
        if (task.getCreatedAt() == null) task.setCreatedAt(LocalDateTime.now());
        store.saveTask(task);
    }

    public void registerSuite(EvalSuite suite) {
        if (suite.getCreatedAt() == null) suite.setCreatedAt(LocalDateTime.now());
        store.saveSuite(suite);
    }

    public EvalSuite getSuite(String suiteId) { return store.getSuite(suiteId); }
    public EvalTask getTask(String taskId) { return store.getTask(taskId); }
    public List<EvalSuite> listSuites() { return store.listSuites(); }
    public List<EvalTask> listTasks() { return store.listTasks(); }
    public List<EvalTrial> getTrialsByTask(String taskId) { return store.getTrialsByTask(taskId); }
}