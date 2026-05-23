package com.yancy.yharness.eval.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class EvalTrial {
    private String id;
    private String taskId;
    private int attemptNumber;
    private EvalTranscript transcript;
    private Map<String, GraderResult> graderResults;
    private boolean passed;
    private String failureReason;
    private EvalOutcome outcome;
    private long elapsedMs;
    private int toolCallCount;
    private int tokenUsage;
    private LocalDateTime executedAt;
}