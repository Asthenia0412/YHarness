
package com.example.yharness.hooks;

import com.example.yharness.context.AgentContext;
import com.example.yharness.core.Action;
import com.example.yharness.core.Thought;
import com.example.yharness.config.AgentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HookManager {

    private static final Logger logger = LoggerFactory.getLogger(HookManager.class);
    
    private final List<AgentHook> hooks = new ArrayList<>();
    private final boolean enabled;

    public HookManager(AgentProperties agentProperties) {
        this.enabled = agentProperties.getHooks().isEnabled();
        logger.info("Hooks enabled: {}", enabled);
    }

    public void registerHook(AgentHook hook) {
        hooks.add(hook);
        logger.debug("Registered hook: {}", hook.getClass().getSimpleName());
    }

    public void onAgentStart(AgentContext context) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onAgentStart(context);
            } catch (Exception e) {
                logger.error("Error in hook onAgentStart", e);
            }
        }
    }

    public void onAgentEnd(AgentContext context) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onAgentEnd(context);
            } catch (Exception e) {
                logger.error("Error in hook onAgentEnd", e);
            }
        }
    }

    public void onContextInit(AgentContext context) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onContextInit(context);
            } catch (Exception e) {
                logger.error("Error in hook onContextInit", e);
            }
        }
    }

    public void onContextUpdate(AgentContext context) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onContextUpdate(context);
            } catch (Exception e) {
                logger.error("Error in hook onContextUpdate", e);
            }
        }
    }

    public void onMessageReceived(AgentContext context, String message) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onMessageReceived(context, message);
            } catch (Exception e) {
                logger.error("Error in hook onMessageReceived", e);
            }
        }
    }

    public void onMessageSend(AgentContext context, String message) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onMessageSend(context, message);
            } catch (Exception e) {
                logger.error("Error in hook onMessageSend", e);
            }
        }
    }

    public void onToolCall(AgentContext context, Action action) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onToolCall(context, action);
            } catch (Exception e) {
                logger.error("Error in hook onToolCall", e);
            }
        }
    }

    public void onToolResult(AgentContext context, String result) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onToolResult(context, result);
            } catch (Exception e) {
                logger.error("Error in hook onToolResult", e);
            }
        }
    }

    public void onProviderCall(AgentContext context) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onProviderCall(context);
            } catch (Exception e) {
                logger.error("Error in hook onProviderCall", e);
            }
        }
    }

    public void onProviderResponse(AgentContext context, String response) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onProviderResponse(context, response);
            } catch (Exception e) {
                logger.error("Error in hook onProviderResponse", e);
            }
        }
    }

    public void onError(AgentContext context, Exception exception) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onError(context, exception);
            } catch (Exception e) {
                logger.error("Error in hook onError", e);
            }
        }
    }

    public void onReActStart(AgentContext context) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onReActStart(context);
            } catch (Exception e) {
                logger.error("Error in hook onReActStart", e);
            }
        }
    }

    public void onReActEnd(AgentContext context, long durationMs) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onReActEnd(context, durationMs);
            } catch (Exception e) {
                logger.error("Error in hook onReActEnd", e);
            }
        }
    }

    public void onThoughtGenerated(AgentContext context, Thought thought) {
        if (!enabled) return;
        for (AgentHook hook : hooks) {
            try {
                hook.onThoughtGenerated(context, thought);
            } catch (Exception e) {
                logger.error("Error in hook onThoughtGenerated", e);
            }
        }
    }

    public int getHookCount() {
        return hooks.size();
    }
}
