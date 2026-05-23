package com.yancy.yharness.eval.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraderResult {
    private String graderName;
    private EvalTask.GraderType type;
    private boolean passed;
    private double score;
    private String details;
    private List<String> failedAssertions;
    private Map<String, Object> metrics;
}