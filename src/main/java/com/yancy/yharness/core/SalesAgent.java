package com.yancy.yharness.core;

import com.yancy.yharness.config.AgentProperties;
import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.context.ContextAssembler;
import com.yancy.yharness.hooks.HookManager;
import com.yancy.yharness.model.*;
import com.yancy.yharness.provider.ModelProvider;
import com.yancy.yharness.provider.ProviderFactory;
import com.yancy.yharness.tools.ToolDefinition;
import com.yancy.yharness.tools.registry.ToolDispatcher;
import com.yancy.yharness.tools.ToolExecutionContext;
import com.yancy.yharness.tools.ToolResult;
import com.yancy.yharness.eval.EvalTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SalesAgent implements Agent {
    private static final Logger log = LoggerFactory.getLogger(SalesAgent.class);

    private final AgentProperties agentProperties;
    private final AgentConfig agentConfig;
    private final ContextAssembler contextAssembler;
    private final HookManager hookManager;
    private final ProviderFactory providerFactory;
    private final ToolDispatcher toolDispatcher;
    private final ObjectProvider<EvalTarget> evalTargetProvider;
    private final ToolExecutor toolExecutor;

    public SalesAgent(AgentProperties agentProperties,
                      ContextAssembler contextAssembler,
                      HookManager hookManager,
                      ProviderFactory providerFactory,
                      ToolDispatcher toolDispatcher,
                      ObjectProvider<EvalTarget> evalTargetProvider,
                      com.yancy.yharness.tools.registry.BaseToolRegistry toolRegistry) {
        this.agentProperties = agentProperties;
        this.agentConfig = buildAgentConfig();
        this.contextAssembler = contextAssembler;
        this.hookManager = hookManager;
        this.providerFactory = providerFactory;
        this.toolDispatcher = toolDispatcher;
        this.evalTargetProvider = evalTargetProvider;
        this.toolExecutor = buildToolExecutor();
    }

    @Override
    public AgentResponse handle(AgentRequest request) {
        long startTime = System.currentTimeMillis();

        AgentState state = new AgentState();
        state.initFromRequest(request);

        ContextAssembler.AssemblyResult assembly = contextAssembler.assemble(request);
        AgentContext context = assembly.getContext();

        hookManager.onSessionStart(context, state);
        hookManager.onContextAssembling(context);
        hookManager.onMemoryRetrieved(context);

        ModelProvider provider = providerFactory.getProvider(agentProperties.getProvider().getType());
        List<ToolDefinition> toolDefs = context.getVisibleTools();

        ReActLoop reactLoop = new ReActLoop(
                provider,
                agentProperties.getReact().getMaxIterations()
        );

        ReActLoop.ReActInput reactInput = new ReActLoop.ReActInput();
        reactInput.setState(state);
        reactInput.setSystemPrompt(context.buildContextString());
        reactInput.setUserMessage(request.getUserMessage());
        reactInput.setToolDefinitions(toolDefs);
        reactInput.setToolExecutor(toolExecutor);
        reactInput.setTemperature(agentProperties.getProvider().getTemperature());
        reactInput.setMaxTokens(agentProperties.getProvider().getMaxTokens());

        hookManager.onBeforeModelCall(context, state);
        ReActLoop.ReActResult reactResult = reactLoop.execute(reactInput);
        hookManager.onAfterModelCall(context, state, reactResult.getFinalAnswer());

        state.getOutputState().setFinalAnswer(reactResult.getFinalAnswer());
        state.getPerfState().setTokenUsage(reactResult.getTokenUsage());

        hookManager.onBeforeSessionSummarize(context, state);
        contextAssembler.finalizeSession(state, assembly, request);
        hookManager.onConversationUpdated(context);
        hookManager.onStoryUpdated(context);
        hookManager.onSessionEnd(context, state);

        AgentResponse response = new AgentResponse();
        response.setSessionId(context.getSessionId());
        response.setFinalReply(reactResult.getFinalAnswer());
        response.setToolCalls(reactResult.getToolCallRecords());
        response.setTokenUsage(reactResult.getTokenUsage());
        response.setElapsedMs(System.currentTimeMillis() - startTime);

        return response;
    }

    @Override
    public AgentConfig getConfig() {
        return agentConfig;
    }

    @Override
    public EvalTarget getEvalTarget() {
        return evalTargetProvider.getObject();
    }

    private AgentConfig buildAgentConfig() {
        AgentConfig config = new AgentConfig();
        config.setAgentId(agentProperties.getAgent().getAgentId());

        AgentConfig.ModelConfig modelConfig = new AgentConfig.ModelConfig();
        modelConfig.setProvider(agentProperties.getProvider().getType());
        modelConfig.setModelName(agentProperties.getProvider().getModel());
        modelConfig.setTemperature(agentProperties.getProvider().getTemperature());
        modelConfig.setMaxTokens(agentProperties.getProvider().getMaxTokens());
        config.setModelConfig(modelConfig);

        AgentConfig.ReActConfig reactConfig = new AgentConfig.ReActConfig();
        reactConfig.setMaxIterations(agentProperties.getReact().getMaxIterations());
        config.setReactConfig(reactConfig);

        return config;
    }

    private ToolExecutor buildToolExecutor() {
        return (toolName, argumentsJson, state) -> {
            ToolExecutionContext ctx = new ToolExecutionContext();
            ctx.setUserId(state.getInputState().getUserId());
            ctx.setConversationId(state.getInputState().getConversationId());
            ctx.setSessionId(state.getInputState().getConversationId());
            ctx.setRequestTime(LocalDateTime.now());
            ctx.setLanguage(state.getInputState().getLanguageCode());
            ctx.setCallSource("react_loop");

            Map<String, Object> args = new HashMap<>();
            if (argumentsJson != null && !argumentsJson.isEmpty()) {
                try {
                    args = parseJson(argumentsJson);
                } catch (Exception e) {
                    args.put("query", argumentsJson);
                }
            }

            ToolResult result = toolDispatcher.dispatch(toolName, ctx, args);
            return result.getSummary() != null ? result.getSummary() : result.getMessage();
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, Map.class);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("query", json);
            return result;
        }
    }
}