
package com.yancy.yharness.context;

import com.yancy.yharness.config.AgentProperties;
import com.yancy.yharness.provider.AIProvider;
import com.yancy.yharness.provider.ProviderFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContextManager {

    private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);
    private static final String SUMMARY_PREFIX = "【历史摘要】";

    private final AgentProperties agentProperties;
    private final ObjectMapper objectMapper;
    private final ProviderFactory providerFactory;
    private final Map<String, AgentContext> contextCache = new HashMap<>();

    public ContextManager(AgentProperties agentProperties, 
                         ObjectMapper objectMapper,
                         ProviderFactory providerFactory) {
        this.agentProperties = agentProperties;
        this.objectMapper = objectMapper;
        this.providerFactory = providerFactory;
    }

    public AgentContext createContext() {
        AgentContext context = new AgentContext();
        context.setSystemPrompt(agentProperties.getContext().getSystemPrompt());
        context.setConversationId(java.util.UUID.randomUUID().toString());
        contextCache.put(context.getConversationId(), context);
        logger.debug("Created new context with id: {}", context.getConversationId());
        return context;
    }

    public AgentContext getContext(String conversationId) {
        return contextCache.get(conversationId);
    }

    public void addMessage(String conversationId, Message message) {
        AgentContext context = contextCache.get(conversationId);
        if (context != null) {
            context.addMessage(message);
            compressIfNeeded(context);
        }
    }

    private void compressIfNeeded(AgentContext context) {
        if (!agentProperties.getContext().isCompressionEnabled()) {
            return;
        }

        int triggerSize = agentProperties.getContext().getCompressTriggerSize();
        int windowSize = agentProperties.getContext().getSlidingWindowSize();

        if (context.getMessages().size() > triggerSize) {
            compress(context, windowSize);
        }
    }

    private void compress(AgentContext context, int windowSize) {
        List<Message> messages = context.getMessages();
        int preserveCount = messages.size() - windowSize;

        if (preserveCount <= 0) {
            return;
        }

        List<Message> toCompress = new ArrayList<>(messages.subList(0, preserveCount));
        logger.info("Compressing {} messages, keeping {} in sliding window", toCompress.size(), windowSize);

        String summary = generateSummary(toCompress);

        extractToLongTermMemory(context, toCompress);

        List<Message> recentMessages = new ArrayList<>(messages.subList(preserveCount, messages.size()));
        messages.clear();
        messages.add(new Message(MessageRole.SYSTEM, SUMMARY_PREFIX + summary));
        messages.addAll(recentMessages);

        logger.info("Compression complete. Total messages now: {}", messages.size());
    }

    private String generateSummary(List<Message> messages) {
        StringBuilder content = new StringBuilder();
        for (Message msg : messages) {
            content.append(String.format("[%s] %s: %s\n", 
                msg.getRole().getValue(), 
                msg.getName() != null ? msg.getName() : "",
                truncate(msg.getContent(), 200)));
        }

        AIProvider provider = providerFactory.getProvider();
        AgentContext tempContext = new AgentContext();
        tempContext.setSystemPrompt("你是一个对话摘要助手。请简洁地总结以下对话的要点，保留关键信息（用户偏好、已分析的内容、结论等）。直接返回摘要，不要有其他内容。");
        tempContext.addMessage(new Message(MessageRole.USER, "请总结以下对话：\n" + content.toString()));

        try {
            String summary = provider.generate(tempContext);
            logger.debug("Generated summary: {}", summary);
            return summary;
        } catch (Exception e) {
            logger.error("Failed to generate summary, using fallback", e);
            return generateFallbackSummary(messages);
        }
    }

    private String generateFallbackSummary(List<Message> messages) {
        int userMsgs = 0;
        int assistantMsgs = 0;
        int toolCalls = 0;
        List<String> topics = new ArrayList<>();

        for (Message msg : messages) {
            switch (msg.getRole()) {
                case USER:
                    userMsgs++;
                    break;
                case ASSISTANT:
                    assistantMsgs++;
                    break;
                case TOOL:
                    toolCalls++;
                    break;
            }
            if (msg.getContent() != null && msg.getContent().length() > 10) {
                topics.add(truncate(msg.getContent(), 50));
            }
        }

        return String.format("对话包含%d条用户消息、%d条助手回复、%d次工具调用。关键内容：%s",
            userMsgs, assistantMsgs, toolCalls, String.join("; ", topics.subList(0, Math.min(3, topics.size()))));
    }

    private void extractToLongTermMemory(AgentContext context, List<Message> messages) {
        for (Message msg : messages) {
            if (msg.getRole() == MessageRole.USER && msg.getContent() != null) {
                if (containsPreference(msg.getContent())) {
                    context.getLongTermMemory().addItem(new LongTermMemory.MemoryItem(
                        "user_preference",
                        extractPreference(msg.getContent()),
                        "preference"
                    ));
                }
            }

            if (msg.getRole() == MessageRole.TOOL && msg.getName() != null) {
                context.getLongTermMemory().addItem(new LongTermMemory.MemoryItem(
                    "tool_result_" + msg.getName(),
                    truncate(msg.getContent(), 500),
                    "tool_result"
                ));
            }
        }

        logger.debug("Long-term memory now has {} items", context.getLongTermMemory().getItems().size());
    }

    private boolean containsPreference(String content) {
        String lower = content.toLowerCase();
        return lower.contains("喜欢") || lower.contains("偏好") || 
               lower.contains("想") || lower.contains("要") ||
               lower.contains("关注") || lower.contains("投资风格");
    }

    private String extractPreference(String content) {
        return truncate(content, 200);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }

    private void trimMessages(AgentContext context) {
        int maxMessages = agentProperties.getContext().getMaxMessages();
        List<Message> messages = context.getMessages();
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    public void updateToolResult(String conversationId, String result) {
        AgentContext context = contextCache.get(conversationId);
        if (context != null) {
            context.setToolExecutionResult(result);
        }
    }

    public void addToolDefinition(String conversationId, ToolDefinition toolDefinition) {
        AgentContext context = contextCache.get(conversationId);
        if (context != null) {
            context.addToolDefinition(toolDefinition);
        }
    }

    public String getPromptForOpenAI(AgentContext context) {
        try {
            Map<String, Object> prompt = new HashMap<>();
            prompt.put("system", context.getSystemPrompt());
            
            if (context.getToolExecutionResult() != null) {
                prompt.put("tool_result", context.getToolExecutionResult());
            }
            
            if (!context.getLongTermMemory().getItems().isEmpty()) {
                prompt.put("memory", context.getLongTermMemory().getItems());
            }
            
            if (!context.getToolDefinitions().isEmpty()) {
                prompt.put("tools", context.getToolDefinitions());
            }
            
            return objectMapper.writeValueAsString(prompt);
        } catch (JsonProcessingException e) {
            logger.error("Error building OpenAI prompt", e);
            return context.getSystemPrompt();
        }
    }

    public String getPromptForAnthropic(AgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getSystemPrompt());
        
        if (!context.getToolDefinitions().isEmpty()) {
            sb.append("\n\n可用工具：\n");
            for (ToolDefinition tool : context.getToolDefinitions()) {
                sb.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
            }
        }
        
        if (context.getToolExecutionResult() != null) {
            sb.append("\n\n工具执行结果：\n").append(context.getToolExecutionResult());
        }
        
        return sb.toString();
    }

    public void clear(String conversationId) {
        contextCache.remove(conversationId);
    }

    public void clearAll() {
        contextCache.clear();
    }
}
