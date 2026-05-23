package com.yancy.yharness.eval.rpc;

import com.yancy.yharness.eval.metrics.EvalMetrics;
import com.yancy.yharness.eval.model.EvalSuite;
import com.yancy.yharness.eval.pipeline.PreDeploymentValidator;
import com.yancy.yharness.eval.service.EvalService;
import com.yancy.yharness.eval.feedback.FeedbackEntry;
import com.yancy.yharness.eval.feedback.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eval")
public class EvalController {
    private static final Logger log = LoggerFactory.getLogger(EvalController.class);
    private final EvalService evalService;
    private final MockEvalPlatformService platformService;
    private final PreDeploymentValidator preDeployValidator;
    private final FeedbackService feedbackService;

    public EvalController(EvalService evalService, MockEvalPlatformService platformService,
                          PreDeploymentValidator preDeployValidator, FeedbackService feedbackService) {
        this.evalService = evalService;
        this.platformService = platformService;
        this.preDeployValidator = preDeployValidator;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/suites")
    public List<EvalSuite> listSuites() {
        return evalService.listSuites();
    }

    @GetMapping("/tasks")
    public List<com.yancy.yharness.eval.model.EvalTask> listTasks() {
        return evalService.listTasks();
    }

    @PostMapping("/run/{suiteId}")
    public Map<String, Object> runSuite(@PathVariable String suiteId) {
        EvalSuite suite = evalService.getSuite(suiteId);
        if (suite == null) {
            return Map.of("error", "Suite not found: " + suiteId);
        }
        EvalMetrics metrics = evalService.runSuite(suite);
        return Map.of(
                "suiteId", suiteId,
                "passRate", metrics.getPassRate(),
                "avgScore", metrics.getAvgScore(),
                "passed", metrics.getPassedTasks(),
                "total", metrics.getTotalTasks(),
                "failures", metrics.getFailures()
        );
    }

    @PostMapping("/run-task/{taskId}")
    public Map<String, Object> runTask(@PathVariable String taskId,
                                        @RequestParam(defaultValue = "3") int trials) {
        List<com.yancy.yharness.eval.model.EvalTrial> results = evalService.runTask(taskId, trials);
        long passed = results.stream().filter(com.yancy.yharness.eval.model.EvalTrial::isPassed).count();
        return Map.of(
                "taskId", taskId,
                "trials", results.size(),
                "passed", passed,
                "passAt1", results.isEmpty() ? 0 : (double) passed / results.size()
        );
    }

    @PostMapping("/validate/{suiteId}")
    public Map<String, Object> validatePreDeploy(@PathVariable String suiteId) {
        PreDeploymentValidator.ValidationResult result = preDeployValidator.validate(suiteId);
        return Map.of(
                "passed", result.isPassed(),
                "message", result.getMessage(),
                "retriesUsed", result.getRetriesUsed(),
                "metrics", result.getMetrics() != null ? Map.of(
                        "passRate", result.getMetrics().getPassRate(),
                        "avgScore", result.getMetrics().getAvgScore()
                ) : Map.of()
        );
    }

    @PostMapping("/platform")
    public EvalPlatformResponse platformCall(@RequestBody EvalPlatformRequest request) {
        log.info("[EvalController] Platform request: action={}", request.getAction());
        return platformService.handle(request);
    }

    @PostMapping("/feedback")
    public Map<String, Object> submitFeedback(@RequestBody FeedbackEntry entry) {
        FeedbackEntry saved = feedbackService.submitFeedback(entry);
        return Map.of(
                "feedbackId", saved.getId(),
                "status", "acknowledged"
        );
    }

    @PostMapping("/feedback/enrich")
    public Map<String, Object> enrichFromFeedback() {
        List<com.yancy.yharness.eval.model.EvalTask> newTasks = feedbackService.enrichEvalSetFromFeedback();
        return Map.of(
                "newTasksCreated", newTasks.size(),
                "taskIds", newTasks.stream().map(com.yancy.yharness.eval.model.EvalTask::getId).toList()
        );
    }

    @GetMapping("/trials/{taskId}")
    public List<com.yancy.yharness.eval.model.EvalTrial> getTrials(@PathVariable String taskId) {
        return evalService.getTrialsByTask(taskId);
    }
}