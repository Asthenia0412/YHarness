package com.yancy.yharness.eval.model;

import lombok.Data;

import java.util.Map;

@Data
public class EvalOutcome {
    private boolean goalAchieved;
    private Map<String, Object> stateSnapshot;
    private String summary;
}