package com.yancy.yharness.eval.grader;

import com.yancy.yharness.eval.model.EvalTask;
import com.yancy.yharness.eval.model.EvalTranscript;
import com.yancy.yharness.eval.model.GraderResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CompositeGrader implements Grader {
    private final List<Grader> graders;

    public CompositeGrader(List<Grader> graders) {
        this.graders = graders;
    }

    @Override
    public String name() { return "CompositeGrader"; }

    @Override
    public GraderResult grade(EvalTask task, EvalTranscript transcript) {
        GraderResult composite = new GraderResult();
        composite.setGraderName(name());
        composite.setType(null);

        Map<String, Object> allMetrics = new HashMap<>();
        double totalScore = 0;
        double totalWeight = 0;
        boolean allPassed = true;
        List<String> allFailures = new ArrayList<>();
        List<GraderResult> subResults = new ArrayList<>();

        for (EvalTask.GraderConfig config : task.getGraders()) {
            for (Grader grader : graders) {
                if (grader.supports(config.getType())) {
                    GraderResult subResult = grader.grade(task, transcript);
                    subResults.add(subResult);
                    double weight = config.getWeight() > 0 ? config.getWeight() : 1.0;
                    totalScore += subResult.getScore() * weight;
                    totalWeight += weight;
                    if (!subResult.isPassed()) {
                        allPassed = false;
                        if (subResult.getFailedAssertions() != null) {
                            allFailures.addAll(subResult.getFailedAssertions());
                        }
                    }
                    if (subResult.getMetrics() != null) {
                        allMetrics.putAll(subResult.getMetrics());
                    }
                }
            }
        }

        composite.setPassed(allPassed);
        composite.setScore(totalWeight > 0 ? totalScore / totalWeight : 1.0);
        composite.setFailedAssertions(allFailures);
        composite.setMetrics(allMetrics);
        composite.setDetails(allPassed ? "All graders passed (score: " + composite.getScore() + ")"
                : "Composite failed with score: " + composite.getScore());
        return composite;
    }

    @Override
    public boolean supports(EvalTask.GraderType type) {
        return true;
    }
}