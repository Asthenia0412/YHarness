package com.yancy.yharness.eval.pipeline;

import com.yancy.yharness.eval.metrics.EvalMetrics;
import com.yancy.yharness.eval.model.EvalSuite;
import com.yancy.yharness.eval.service.EvalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PreDeploymentValidator {
    private static final Logger log = LoggerFactory.getLogger(PreDeploymentValidator.class);
    private final EvalService evalService;

    private static final double PASS_THRESHOLD = 0.8;
    private static final int MAX_RETRIES = 3;

    public PreDeploymentValidator(EvalService evalService) {
        this.evalService = evalService;
    }

    public ValidationResult validate(String suiteId) {
        return validateWithRetries(suiteId, 0);
    }

    private ValidationResult validateWithRetries(String suiteId, int retryCount) {
        log.info("[PreDeploy] Starting validation for suite: {} (attempt {}/{})",
                suiteId, retryCount + 1, MAX_RETRIES);

        EvalSuite suite = evalService.getSuite(suiteId);
        if (suite == null) {
            return ValidationResult.fail("Suite not found: " + suiteId);
        }

        EvalMetrics metrics = evalService.runSuite(suite);
        boolean passed = metrics.getPassRate() >= PASS_THRESHOLD;

        if (passed) {
            log.info("[PreDeploy] SUITE PASSED: {} - passRate={}, avgScore={}",
                    suiteId, metrics.getPassRate(), metrics.getAvgScore());
            return ValidationResult.pass(metrics);
        }

        log.warn("[PreDeploy] SUITE FAILED: {} - passRate={} (threshold={}), attempt={}/{}",
                suiteId, metrics.getPassRate(), PASS_THRESHOLD, retryCount + 1, MAX_RETRIES);

        if (retryCount < MAX_RETRIES - 1) {
            log.info("[PreDeploy] Retrying suite {} (attempt {})", suiteId, retryCount + 2);
            return validateWithRetries(suiteId, retryCount + 1);
        }

        log.error("[PreDeploy] All {} retries exhausted for suite {}. "
                        + "Consider architecture review. Final passRate={}",
                MAX_RETRIES, suiteId, metrics.getPassRate());

        return ValidationResult.failAfterRetries(metrics, MAX_RETRIES);
    }

    public static class ValidationResult {
        private final boolean passed;
        private final EvalMetrics metrics;
        private final String message;
        private final int retriesUsed;

        private ValidationResult(boolean passed, EvalMetrics metrics, String message, int retriesUsed) {
            this.passed = passed;
            this.metrics = metrics;
            this.message = message;
            this.retriesUsed = retriesUsed;
        }

        public static ValidationResult pass(EvalMetrics metrics) {
            return new ValidationResult(true, metrics,
                    "Validation passed with rate: " + metrics.getPassRate(), 0);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, null, message, 0);
        }

        public static ValidationResult failAfterRetries(EvalMetrics metrics, int retries) {
            return new ValidationResult(false, metrics,
                    "Validation failed after " + retries + " retries. Last pass rate: "
                            + metrics.getPassRate() + ". Consider architecture review.", retries);
        }

        public boolean isPassed() { return passed; }
        public EvalMetrics getMetrics() { return metrics; }
        public String getMessage() { return message; }
        public int getRetriesUsed() { return retriesUsed; }
    }
}