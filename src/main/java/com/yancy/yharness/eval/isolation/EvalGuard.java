package com.yancy.yharness.eval.isolation;

import com.yancy.yharness.eval.model.EvalOutcome;
import com.yancy.yharness.eval.model.EvalTranscript;
import com.yancy.yharness.tools.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EvalGuard {
    private static final Logger log = LoggerFactory.getLogger(EvalGuard.class);

    public ToolResult guardToolExecution(String toolName, Map<String, Object> arguments,
                                          java.util.function.Function<Map<String, Object>, ToolResult> realExecutor) {
        if (!EvalContext.isEvalMode()) {
            return realExecutor.apply(arguments);
        }

        log.info("[EVAL_GUARD] Tool '{}' intercepted in eval mode", toolName);

        if (isWriteOperation(toolName)) {
            log.warn("[EVAL_GUARD] Write operation '{}' blocked in eval mode", toolName);
            return ToolResult.failure("EVAL_BLOCKED",
                    "Write operation blocked in eval mode. Tool: " + toolName, false);
        }

        return realExecutor.apply(arguments);
    }

    public boolean isWriteOperation(String toolName) {
        return toolName.startsWith("create")
                || toolName.startsWith("update")
                || toolName.startsWith("delete")
                || toolName.startsWith("send")
                || toolName.startsWith("process");
    }

    public EvalTranscript sanitizeTranscript(EvalTranscript transcript) {
        return transcript;
    }

    public EvalOutcome captureOutcome(EvalTranscript transcript) {
        EvalOutcome outcome = new EvalOutcome();
        Map<String, Object> state = new HashMap<>();
        if (transcript.getEntries() != null && !transcript.getEntries().isEmpty()) {
            EvalTranscript.TranscriptEntry last = transcript.getEntries()
                    .get(transcript.getEntries().size() - 1);
            state.put("last_role", last.getRole());
            state.put("has_tool_result", last.getToolResult() != null);
            outcome.setGoalAchieved("assistant".equals(last.getRole()));
        } else {
            outcome.setGoalAchieved(false);
        }
        outcome.setStateSnapshot(state);
        outcome.setSummary("Eval mode outcome captured");
        return outcome;
    }
}