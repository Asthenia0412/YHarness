package com.yancy.yharness.eval;

import com.yancy.yharness.context.AgentContext;

import java.util.Map;

public interface EvalTarget {
    String id();
    String name();
    EvalInvokeResult invoke(AgentContext ctx, Map<String, Object> evalInput);
}