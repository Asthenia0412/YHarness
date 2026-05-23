package com.yancy.yharness.pipeline;

import com.yancy.yharness.core.Agent;
import com.yancy.yharness.eval.isolation.EvalContext;
import com.yancy.yharness.eval.isolation.EvalGuard;
import com.yancy.yharness.model.AgentRequest;
import com.yancy.yharness.model.AgentResponse;
import com.yancy.yharness.model.TaskType;
import com.yancy.yharness.preparer.RequestPreparer;
import com.yancy.yharness.scheduler.ClientTaskJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DispatchPipeline {
    private static final Logger log = LoggerFactory.getLogger(DispatchPipeline.class);

    private final RequestPreparer requestPreparer;
    private final Agent agent;
    private final EvalGuard evalGuard;

    public DispatchPipeline(RequestPreparer requestPreparer, Agent agent, EvalGuard evalGuard) {
        this.requestPreparer = requestPreparer;
        this.agent = agent;
        this.evalGuard = evalGuard;
    }

    public AgentResponse dispatchChat(String userId, String message, String conversationId) {
        return dispatchChat(userId, message, conversationId, null);
    }

    public AgentResponse dispatchChat(String userId, String message, String conversationId,
                                       Map<String, Object> metadata) {
        DispatchContext ctx = DispatchContext.production();
        AgentRequest request = requestPreparer.prepareFromMessage(userId, message, conversationId);
        if (metadata != null) {
            if (request.getMetadata() == null) {
                request.setMetadata(new HashMap<>());
            }
            request.getMetadata().putAll(metadata);
        }
        return execute(request, ctx);
    }

    public AgentResponse dispatchJob(ClientTaskJob job) {
        DispatchContext ctx = DispatchContext.production();
        AgentRequest request = requestPreparer.prepare(job);
        return execute(request, ctx);
    }

    public AgentResponse dispatchEval(String userId, String message, String conversationId) {
        DispatchContext ctx = DispatchContext.eval();
        AgentRequest request = requestPreparer.prepareFromMessage(userId, message, conversationId);
        if (request.getMetadata() == null) {
            request.setMetadata(new HashMap<>());
        }
        request.getMetadata().put("eval_mode", "true");
        request.getMetadata().put("eval_user_id", userId);
        return execute(request, ctx);
    }

    public AgentResponse dispatchDirect(AgentRequest request) {
        return execute(request, DispatchContext.production());
    }

    private AgentResponse execute(AgentRequest request, DispatchContext ctx) {
        boolean isEval = ctx.isEvalMode();

        if (isEval) {
            EvalContext.enter(request.getUserId());
            log.debug("[DISPATCH] Eval mode activated for user: {}", request.getUserId());
        }

        try {
            AgentRequest prepared = preProcess(request, ctx);
            log.debug("[DISPATCH] Phase 1 (Dispatch) completed: mode={}", isEval ? "eval" : "production");
            log.debug("[DISPATCH] Phase 2 (Pre-process) completed: userId={}, taskType={}",
                    prepared.getUserId(), prepared.getTaskType());

            AgentResponse response = agent.handle(prepared);

            log.debug("[DISPATCH] Phase 3 (Execute) completed: elapsed={}ms",
                    response.getElapsedMs());
            return response;
        } finally {
            if (isEval) {
                EvalContext.exit();
                log.debug("[DISPATCH] Eval context cleaned up");
            }
        }
    }

    private AgentRequest preProcess(AgentRequest request, DispatchContext ctx) {
        if (ctx.isEvalMode()) {
            if (request.getMetadata() == null) {
                request.setMetadata(new HashMap<>());
            }
            request.getMetadata().put("eval_mode", "true");
        }

        if (request.getTaskType() == null) {
            request.setTaskType(TaskType.INBOUND);
        }
        if (request.getLanguageCode() == null) {
            request.setLanguageCode("en");
        }
        if (request.getChannelId() == null) {
            request.setChannelId("api");
        }
        if (request.getChannelAccountId() == null) {
            request.setChannelAccountId("default");
        }
        if (request.getTimezone() == null) {
            request.setTimezone("Asia/Bangkok");
        }

        return request;
    }

    public static class DispatchContext {
        private final boolean evalMode;

        private DispatchContext(boolean evalMode) {
            this.evalMode = evalMode;
        }

        public static DispatchContext production() {
            return new DispatchContext(false);
        }

        public static DispatchContext eval() {
            return new DispatchContext(true);
        }

        public boolean isEvalMode() {
            return evalMode;
        }
    }
}