
package com.yancy.yharness.tools;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.exception.AgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ToolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ToolExecutor.class);
    
    private final Map<String, Tool> tools = new HashMap<>();

    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
        logger.info("Registered tool: {}", tool.getName());
    }

    public Tool getTool(String name) {
        return tools.get(name);
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    public String execute(String toolName, AgentContext context, Map<String, Object> arguments) {
        Tool tool = tools.get(toolName);
        if (tool == null) {
            throw new AgentException("Tool not found: " + toolName);
        }
        
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("🔧 工具执行开始");
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("│ 工具名称: {}", toolName);
        logger.info("│ 会话ID: {}", context.getConversationId());
        logger.info("│ 当前销售阶段: {}", context.getBusinessState().getCurrentStage().getDescription());
        logger.info("│─────────────────────────────────────────────────────────────│");
        logger.info("│ 执行参数:");
        
        if (arguments != null && !arguments.isEmpty()) {
            for (Map.Entry<String, Object> entry : arguments.entrySet()) {
                logger.info("│   ├─ {}: {}", entry.getKey(), entry.getValue());
            }
        } else {
            logger.info("│   (无参数)");
        }
        
        logger.info("═══════════════════════════════════════════════════════════════");
        
        long startTime = System.currentTimeMillis();
        try {
            String result = tool.execute(context, arguments);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("✅ 工具执行成功");
            logger.info("│ 执行耗时: {} ms", duration);
            logger.info("│ 结果长度: {} characters", result != null ? result.length() : 0);
            logger.info("│─────────────────────────────────────────────────────────────│");
            logger.info("│ 结果预览:");
            
            if (result != null && !result.isEmpty()) {
                String preview = result.length() > 200 ? result.substring(0, 200) + "..." : result;
                for (String line : preview.split("\n")) {
                    logger.info("│   {}", line);
                }
            } else {
                logger.info("│   (无返回结果)");
            }
            
            logger.info("═══════════════════════════════════════════════════════════════");
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("═══════════════════════════════════════════════════════════════");
            logger.error("❌ 工具执行失败");
            logger.error("│ 执行耗时: {} ms", duration);
            logger.error("│ 错误信息: {}", e.getMessage());
            logger.error("│─────────────────────────────────────────────────────────────│");
            logger.error("│ 堆栈信息:");
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error("│   at {}", element.toString());
            }
            logger.error("═══════════════════════════════════════════════════════════════");
            throw new AgentException("Error executing tool: " + toolName, e);
        }
    }

    public Map<String, Tool> getAllTools() {
        return new HashMap<>(tools);
    }
}
