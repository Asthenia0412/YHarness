
package com.yancy.yharness.controller;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.context.ContextManager;
import com.yancy.yharness.core.ReActEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    
    private final ContextManager contextManager;
    private final ReActEngine reactEngine;

    public AgentController(ContextManager contextManager, ReActEngine reactEngine) {
        this.contextManager = contextManager;
        this.reactEngine = reactEngine;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String conversationId = (String) request.get("conversationId");
            String message = (String) request.get("message");
            
            AgentContext context;
            boolean isNew = false;
            
            if (conversationId == null || conversationId.isEmpty()) {
                context = contextManager.createContext();
                isNew = true;
            } else {
                context = contextManager.getContext(conversationId);
                if (context == null) {
                    context = contextManager.createContext();
                    isNew = true;
                }
            }
            
            reactEngine.registerToolsToContext(context);
            
            String result = reactEngine.start(context, message);
            
            response.put("success", true);
            response.put("conversationId", context.getConversationId());
            response.put("message", result);
            response.put("isNew", isNew);
            
            logger.info("Chat completed for conversation: {}, isNew: {}", context.getConversationId(), isNew);
            
        } catch (Exception e) {
            logger.error("Chat error", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/context/{conversationId}")
    public Map<String, Object> getContext(@PathVariable String conversationId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            AgentContext context = contextManager.getContext(conversationId);
            
            if (context == null) {
                response.put("success", false);
                response.put("error", "Context not found");
            } else {
                response.put("success", true);
                response.put("conversationId", context.getConversationId());
                response.put("messageCount", context.getMessages().size());
                response.put("toolCount", context.getToolDefinitions().size());
                response.put("salesStage", context.getBusinessState().getCurrentStage().getDescription());
            }
            
        } catch (Exception e) {
            logger.error("Get context error", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @DeleteMapping("/context/{conversationId}")
    public Map<String, Object> clearContext(@PathVariable String conversationId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            contextManager.clear(conversationId);
            response.put("success", true);
            
        } catch (Exception e) {
            logger.error("Clear context error", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "YHarness");
        return response;
    }
}
