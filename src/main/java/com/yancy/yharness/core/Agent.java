package com.yancy.yharness.core;

import com.yancy.yharness.model.AgentConfig;
import com.yancy.yharness.model.AgentRequest;
import com.yancy.yharness.model.AgentResponse;
import com.yancy.yharness.eval.EvalTarget;

public interface Agent {
    AgentResponse handle(AgentRequest request);
    AgentConfig getConfig();
    EvalTarget getEvalTarget();
}