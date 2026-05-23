package com.yancy.yharness.eval.rpc;

import lombok.Data;

import java.util.Map;

@Data
public class EvalPlatformRequest {
    private String requestId;
    private String suiteId;
    private String taskId;
    private String userId;
    private EvalAction action;
    private Map<String, Object> params;

    public enum EvalAction {
        RUN_SUITE,
        RUN_TASK,
        GET_RESULT,
        GET_METRICS,
        SUBMIT_FEEDBACK,
        REGISTER_TASK,
        LIST_TASKS
    }
}