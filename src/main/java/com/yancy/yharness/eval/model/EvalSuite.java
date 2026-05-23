package com.yancy.yharness.eval.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class EvalSuite {
    private String id;
    private String name;
    private String description;
    private List<String> taskIds;
    private EvalCategory category;
    private int requiredPassCount;
    private int maxTrialsPerTask;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum EvalCategory {
        QUALITY,
        REGRESSION,
        SAFETY,
        PERFORMANCE
    }

    public enum Verdict {
        PASS,
        CONDITIONAL_PASS,
        FAIL,
        INCONCLUSIVE
    }
}