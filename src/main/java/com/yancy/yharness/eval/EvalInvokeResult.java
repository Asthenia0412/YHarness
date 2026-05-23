package com.yancy.yharness.eval;

import lombok.Data;

import java.util.Map;

@Data
public class EvalInvokeResult {
    private boolean success;
    private String output;
    private Map<String, Object> metrics;
    private String traceId;
}