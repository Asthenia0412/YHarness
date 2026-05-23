package com.yancy.yharness.eval.grader;

import com.yancy.yharness.eval.model.EvalTask;
import com.yancy.yharness.eval.model.EvalTranscript;
import com.yancy.yharness.eval.model.GraderResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class CodeGrader implements Grader {
    @Override
    public String name() { return "CodeGrader"; }

    @Override
    public GraderResult grade(EvalTask task, EvalTranscript transcript) {
        GraderResult result = new GraderResult();
        result.setGraderName(name());
        result.setType(EvalTask.GraderType.CODE_MATCH);

        List<String> failures = new ArrayList<>();
        int passedAssertions = 0;
        int totalAssertions = 0;

        for (EvalTask.GraderConfig config : task.getGraders()) {
            if (!supports(config.getType())) continue;

            if (config.getAssertions() != null) {
                for (String assertion : config.getAssertions()) {
                    totalAssertions++;
                    if (evaluateStringAssertion(assertion, transcript, config)) {
                        passedAssertions++;
                    } else {
                        failures.add("Assertion failed: " + assertion);
                    }
                }
            }
        }

        if (task.getExpectedOutput() != null) {
            for (Map.Entry<String, Object> entry : task.getExpectedOutput().entrySet()) {
                totalAssertions++;
                boolean match = checkExpectedOutput(entry.getKey(), entry.getValue(), transcript);
                if (match) {
                    passedAssertions++;
                } else {
                    failures.add("Expected output mismatch: " + entry.getKey());
                }
            }
        }

        result.setPassed(failures.isEmpty());
        result.setScore(totalAssertions > 0 ? (double) passedAssertions / totalAssertions : 1.0);
        result.setFailedAssertions(failures);
        result.setDetails(failures.isEmpty() ? "All code-level checks passed"
                : "Failed " + failures.size() + "/" + totalAssertions + " checks");
        return result;
    }

    @Override
    public boolean supports(EvalTask.GraderType type) {
        return type == EvalTask.GraderType.CODE_MATCH
                || type == EvalTask.GraderType.CODE_TEST
                || type == EvalTask.GraderType.STATE_CHECK
                || type == EvalTask.GraderType.TOOL_CALL_CHECK
                || type == EvalTask.GraderType.TRANSCRIPT_CONSTRAINT;
    }

    private boolean evaluateStringAssertion(String assertion, EvalTranscript transcript, EvalTask.GraderConfig config) {
        String lastAssistant = "";
        if (transcript.getEntries() != null && !transcript.getEntries().isEmpty()) {
            for (int i = transcript.getEntries().size() - 1; i >= 0; i--) {
                if ("assistant".equals(transcript.getEntries().get(i).getRole())) {
                    lastAssistant = transcript.getEntries().get(i).getContent();
                    break;
                }
            }
        }
        if (assertion.startsWith("regex:")) {
            return Pattern.compile(assertion.substring(6)).matcher(lastAssistant).find();
        }
        return lastAssistant.contains(assertion);
    }

    private boolean checkExpectedOutput(String key, Object expected, EvalTranscript transcript) {
        if (transcript.getFinalState() != null && transcript.getFinalState().containsKey(key)) {
            Object actual = transcript.getFinalState().get(key);
            return expected.equals(actual);
        }
        if (transcript.getEntries() != null) {
            for (EvalTranscript.TranscriptEntry entry : transcript.getEntries()) {
                if (entry.getToolResult() != null && entry.getToolResult().contains(key + ":")) {
                    return entry.getToolResult().contains(String.valueOf(expected));
                }
            }
        }
        return false;
    }
}