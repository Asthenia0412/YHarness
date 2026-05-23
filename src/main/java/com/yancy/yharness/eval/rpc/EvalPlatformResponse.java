package com.yancy.yharness.eval.rpc;

import lombok.Data;

import java.util.Map;

@Data
public class EvalPlatformResponse {
    private String requestId;
    private int statusCode;
    private String statusMessage;
    private Map<String, Object> data;

    public static EvalPlatformResponse success(String requestId, Map<String, Object> data) {
        EvalPlatformResponse resp = new EvalPlatformResponse();
        resp.setRequestId(requestId);
        resp.setStatusCode(200);
        resp.setStatusMessage("OK");
        resp.setData(data);
        return resp;
    }

    public static EvalPlatformResponse error(String requestId, int code, String message) {
        EvalPlatformResponse resp = new EvalPlatformResponse();
        resp.setRequestId(requestId);
        resp.setStatusCode(code);
        resp.setStatusMessage(message);
        return resp;
    }
}