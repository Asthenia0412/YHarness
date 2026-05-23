package com.yancy.yharness.context;

import com.yancy.yharness.memory.Conversation;
import com.yancy.yharness.memory.Story;
import com.yancy.yharness.memory.MemoryService;
import com.yancy.yharness.tools.ToolDefinition;

import java.util.List;

public class AgentContext {
    private String systemPrompt;
    private String userId;
    private String conversationId;
    private String sessionId;
    private String userMessage;
    private Story story;
    private Conversation conversation;
    private List<ToolDefinition> visibleTools;
    private List<String> businessFacts;
    private List<String> knowledgeResults;
    private String outputPolicy;
    private String strategySummary;
    private java.util.Map<String, Object> metadata;

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    public Story getStory() { return story; }
    public void setStory(Story story) { this.story = story; }
    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public List<ToolDefinition> getVisibleTools() { return visibleTools; }
    public void setVisibleTools(List<ToolDefinition> visibleTools) { this.visibleTools = visibleTools; }
    public List<String> getBusinessFacts() { return businessFacts; }
    public void setBusinessFacts(List<String> businessFacts) { this.businessFacts = businessFacts; }
    public List<String> getKnowledgeResults() { return knowledgeResults; }
    public void setKnowledgeResults(List<String> knowledgeResults) { this.knowledgeResults = knowledgeResults; }
    public String getOutputPolicy() { return outputPolicy; }
    public void setOutputPolicy(String outputPolicy) { this.outputPolicy = outputPolicy; }
    public String getStrategySummary() { return strategySummary; }
    public void setStrategySummary(String strategySummary) { this.strategySummary = strategySummary; }
    public java.util.Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }

    public String buildContextString() {
        StringBuilder ctx = new StringBuilder();

        ctx.append("# System\n").append(systemPrompt).append("\n\n");

        ctx.append("# Current Session\n");
        ctx.append("- user_id: ").append(userId).append("\n");
        ctx.append("- conversation_id: ").append(conversationId).append("\n");
        ctx.append("- session_id: ").append(sessionId).append("\n");
        ctx.append("- current user input: ").append(userMessage).append("\n\n");

        if (conversation != null) {
            ctx.append("# Conversation Memory\n");
            ctx.append(conversation.getSummary()).append("\n\n");
        }

        if (story != null) {
            ctx.append("# Story Memory\n");
            ctx.append("- lead_stage: ").append(story.getLeadStage()).append("\n");
            ctx.append("- language: ").append(story.getLanguage()).append("\n");
            ctx.append("- interest_tags: ").append(story.getInterestTags()).append("\n\n");
        }

        if (businessFacts != null && !businessFacts.isEmpty()) {
            ctx.append("# Business Facts\n");
            for (String fact : businessFacts) {
                ctx.append("- ").append(fact).append("\n");
            }
            ctx.append("\n");
        }

        if (knowledgeResults != null && !knowledgeResults.isEmpty()) {
            ctx.append("# Knowledge Retrieval\n");
            for (String kr : knowledgeResults) {
                ctx.append("- ").append(kr).append("\n");
            }
            ctx.append("\n");
        }

        if (visibleTools != null && !visibleTools.isEmpty()) {
            ctx.append("# Available Tools\n");
            for (ToolDefinition td : visibleTools) {
                ctx.append("- ").append(td.getName()).append(": ").append(td.getDescription()).append("\n");
            }
            ctx.append("\n");
        }

        if (outputPolicy != null) {
            ctx.append("# Output Policy\n").append(outputPolicy).append("\n");
        }

        if (strategySummary != null) {
            ctx.append("# Strategy\n").append(strategySummary).append("\n");
        }

        return ctx.toString();
    }
}