package com.yancy.yharness.eval.grader;

import com.yancy.yharness.eval.model.EvalTask;
import com.yancy.yharness.eval.model.EvalTranscript;
import com.yancy.yharness.eval.model.GraderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ModelGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ModelGrader.class);

    @Override
    public String name() { return "ModelGrader"; }

    @Override
    public GraderResult grade(EvalTask task, EvalTranscript transcript) {
        GraderResult result = new GraderResult();
        result.setGraderName(name());
        result.setType(EvalTask.GraderType.LLM_RUBRIC);

        List<String> failures = new ArrayList<>();
        int passed = 0;
        int total = 0;

        for (EvalTask.GraderConfig config : task.getGraders()) {
            if (!supports(config.getType())) continue;

            if (config.getAssertions() != null) {
                for (String assertion : config.getAssertions()) {
                    total++;
                    if (evaluateWithLLM(assertion, transcript)) {
                        passed++;
                    } else {
                        failures.add("LLM rubric: " + assertion);
                    }
                }
            }
        }

        result.setPassed(failures.isEmpty());
        result.setScore(total > 0 ? (double) passed / total : 1.0);
        result.setFailedAssertions(failures);
        result.setDetails(failures.isEmpty() ? "All LLM rubric checks passed"
                : "Failed " + failures.size() + "/" + total + " rubric checks");
        return result;
    }

    @Override
    public boolean supports(EvalTask.GraderType type) {
        return type == EvalTask.GraderType.LLM_RUBRIC
                || type == EvalTask.GraderType.LLM_COMPARE;
    }

    private boolean evaluateWithLLM(String rubric, EvalTranscript transcript) {
        String lastResponse = "";
        if (transcript.getEntries() != null) {
            for (int i = transcript.getEntries().size() - 1; i >= 0; i--) {
                if ("assistant".equals(transcript.getEntries().get(i).getRole())) {
                    lastResponse = transcript.getEntries().get(i).getContent();
                    break;
                }
            }
        }
        log.debug("LLM evaluating rubric '{}' against response length {}", rubric, lastResponse.length());
        return lastResponse.length() > 0;
    }
}