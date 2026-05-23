package com.yancy.yharness.eval.grader;

import com.yancy.yharness.eval.model.EvalTask;
import com.yancy.yharness.eval.model.EvalTranscript;
import com.yancy.yharness.eval.model.GraderResult;

public interface Grader {
    String name();
    GraderResult grade(EvalTask task, EvalTranscript transcript);
    boolean supports(EvalTask.GraderType type);
}