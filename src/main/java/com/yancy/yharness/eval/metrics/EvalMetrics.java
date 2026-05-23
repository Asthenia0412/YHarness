package com.yancy.yharness.eval.metrics;

import lombok.Data;

import java.util.List;

@Data
public class EvalMetrics {
    private int totalTasks;
    private int passedTasks;
    private int failedTasks;
    private double passRate;
    private double avgScore;
    private double avgLatencyMs;
    private double avgTokenUsage;
    private double avgToolCalls;
    private PassAtK passAtK;
    private List<String> failures;

    public static EvalMetrics aggregate(List<EvalTrialSummary> trials) {
        EvalMetrics metrics = new EvalMetrics();
        if (trials == null || trials.isEmpty()) return metrics;

        metrics.setTotalTasks(trials.size());
        metrics.setPassedTasks((int) trials.stream().filter(EvalTrialSummary::isPassed).count());
        metrics.setFailedTasks(metrics.getTotalTasks() - metrics.getPassedTasks());
        metrics.setPassRate((double) metrics.getPassedTasks() / metrics.getTotalTasks());
        metrics.setAvgScore(trials.stream().mapToDouble(EvalTrialSummary::getScore).average().orElse(0));
        metrics.setAvgLatencyMs(trials.stream().mapToLong(EvalTrialSummary::getElapsedMs).average().orElse(0));
        metrics.setAvgTokenUsage(trials.stream().mapToInt(EvalTrialSummary::getTokenUsage).average().orElse(0));
        metrics.setAvgToolCalls(trials.stream().mapToInt(EvalTrialSummary::getToolCallCount).average().orElse(0));

        List<Boolean> passes = trials.stream().map(EvalTrialSummary::isPassed).toList();
        metrics.setPassAtK(PassAtK.calculate(passes, 3));
        metrics.setFailures(trials.stream()
                .filter(t -> !t.isPassed())
                .map(t -> t.getTaskId() + ": " + t.getFailureReason())
                .toList());
        return metrics;
    }

    @Data
    public static class EvalTrialSummary {
        private String taskId;
        private boolean passed;
        private double score;
        private long elapsedMs;
        private int tokenUsage;
        private int toolCallCount;
        private String failureReason;
    }
}