package com.yancy.yharness.eval.rpc;

import com.yancy.yharness.eval.metrics.EvalMetrics;
import com.yancy.yharness.eval.model.EvalSuite;
import com.yancy.yharness.eval.model.EvalTask;
import com.yancy.yharness.eval.model.EvalTrial;
import com.yancy.yharness.eval.service.EvalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MockEvalPlatformService {
    private static final Logger log = LoggerFactory.getLogger(MockEvalPlatformService.class);
    private final EvalService evalService;

    public MockEvalPlatformService(EvalService evalService) {
        this.evalService = evalService;
    }

    public EvalPlatformResponse handle(EvalPlatformRequest request) {
        log.info("[MockEvalPlatform] Received request: action={}, suiteId={}, taskId={}",
                request.getAction(), request.getSuiteId(), request.getTaskId());

        if (request.getAction() == EvalPlatformRequest.EvalAction.RUN_SUITE) {
            return handleRunSuite(request);
        } else if (request.getAction() == EvalPlatformRequest.EvalAction.RUN_TASK) {
            return handleRunTask(request);
        } else if (request.getAction() == EvalPlatformRequest.EvalAction.GET_RESULT) {
            return handleGetResult(request);
        } else if (request.getAction() == EvalPlatformRequest.EvalAction.GET_METRICS) {
            return handleGetMetrics(request);
        } else if (request.getAction() == EvalPlatformRequest.EvalAction.SUBMIT_FEEDBACK) {
            return handleSubmitFeedback(request);
        } else if (request.getAction() == EvalPlatformRequest.EvalAction.REGISTER_TASK) {
            return handleRegisterTask(request);
        } else if (request.getAction() == EvalPlatformRequest.EvalAction.LIST_TASKS) {
            return handleListTasks(request);
        } else {
            return EvalPlatformResponse.error(request.getRequestId(), 400, "Unsupported action: " + request.getAction());
        }
    }

    private EvalPlatformResponse handleRunSuite(EvalPlatformRequest request) {
        String suiteId = request.getSuiteId() != null ? request.getSuiteId() : "suite_default";
        EvalSuite suite = evalService.getSuite(suiteId);
        if (suite == null) {
            return EvalPlatformResponse.error(request.getRequestId(), 404, "Suite not found: " + suiteId);
        }
        EvalMetrics metrics = evalService.runSuite(suite);
        Map<String, Object> data = new HashMap<>();
        data.put("suiteId", suiteId);
        data.put("metrics", metrics);
        data.put("verdict", metrics.getPassRate() >= 0.8 ? "PASS" : "FAIL");
        return EvalPlatformResponse.success(request.getRequestId(), data);
    }

    private EvalPlatformResponse handleRunTask(EvalPlatformRequest request) {
        String taskId = request.getTaskId() != null ? request.getTaskId() : "task_default";
        List<EvalTrial> trials = evalService.runTask(taskId, 3);
        boolean passed = trials.stream().anyMatch(EvalTrial::isPassed);
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("trials", trials.size());
        data.put("passed", passed);
        data.put("pass@1", trials.stream().filter(EvalTrial::isPassed).count() / (double) trials.size());
        return EvalPlatformResponse.success(request.getRequestId(), data);
    }

    private EvalPlatformResponse handleGetResult(EvalPlatformRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("trialId", request.getParams() != null ? request.getParams().get("trialId") : null);
        data.put("status", "completed");
        return EvalPlatformResponse.success(request.getRequestId(), data);
    }

    private EvalPlatformResponse handleGetMetrics(EvalPlatformRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalSuites", evalService.listSuites().size());
        data.put("totalTasks", evalService.listTasks().size());
        return EvalPlatformResponse.success(request.getRequestId(), data);
    }

    private EvalPlatformResponse handleSubmitFeedback(EvalPlatformRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("feedbackId", UUID.randomUUID().toString());
        data.put("status", "acknowledged");
        return EvalPlatformResponse.success(request.getRequestId(), data);
    }

    private EvalPlatformResponse handleRegisterTask(EvalPlatformRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", "task_" + UUID.randomUUID().toString().substring(0, 8));
        data.put("status", "registered");
        return EvalPlatformResponse.success(request.getRequestId(), data);
    }

    private EvalPlatformResponse handleListTasks(EvalPlatformRequest request) {
        List<EvalTask> tasks = evalService.listTasks();
        Map<String, Object> data = new HashMap<>();
        data.put("tasks", tasks.stream().map(t -> Map.of(
                "id", t.getId(),
                "name", t.getName(),
                "category", t.getCategory()
        )).collect(Collectors.toList()));
        data.put("total", tasks.size());
        return EvalPlatformResponse.success(request.getRequestId(), data);
    }
}