package com.yancy.yharness.eval.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class EvalTask {
    private String id;
    private String name;
    private String description;
    private String suiteId;
    private Map<String, Object> input;
    private Map<String, Object> expectedOutput;
    private List<GraderConfig> graders;
    private EvalCategory category;
    private String owner;
    private String source;
    private boolean regressionCandidate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class GraderConfig {
        private GraderType type;
        private String rubricPath;
        private List<String> requiredTests;
        private List<String> assertions;
        private double weight;
        private Map<String, Object> params;
    }

    public enum GraderType {
        CODE_MATCH, CODE_TEST, CODE_STATIC_ANALYSIS,
        LLM_RUBRIC, LLM_COMPARE,
        STATE_CHECK, TOOL_CALL_CHECK,
        TRANSCRIPT_CONSTRAINT,
        HUMAN_REVIEW
    }

    public enum EvalCategory {
        CAPABILITY,
        REGRESSION,
        SAFETY,
        PERFORMANCE
    }
}