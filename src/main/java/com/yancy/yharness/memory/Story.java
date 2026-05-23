package com.yancy.yharness.memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Story {
    private String userId;
    private String leadStage;
    private String language;
    private LocalDateTime lastContactAt;
    private int totalConversations;
    private List<Conversation.SessionSummary> recentConversations;
    private Map<String, String> channelProfiles;
    private Map<String, String> profileAttributes;
    private List<String> interestTags;
    private int version;
    private LocalDateTime updatedAt;

    public Story() {
        this.recentConversations = new ArrayList<>();
        this.channelProfiles = new HashMap<>();
        this.profileAttributes = new HashMap<>();
        this.interestTags = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLeadStage() { return leadStage; }
    public void setLeadStage(String leadStage) { this.leadStage = leadStage; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public LocalDateTime getLastContactAt() { return lastContactAt; }
    public void setLastContactAt(LocalDateTime lastContactAt) { this.lastContactAt = lastContactAt; }
    public int getTotalConversations() { return totalConversations; }
    public void setTotalConversations(int totalConversations) { this.totalConversations = totalConversations; }
    public List<Conversation.SessionSummary> getRecentConversations() { return recentConversations; }
    public void setRecentConversations(List<Conversation.SessionSummary> recentConversations) { this.recentConversations = recentConversations; }
    public Map<String, String> getChannelProfiles() { return channelProfiles; }
    public void setChannelProfiles(Map<String, String> channelProfiles) { this.channelProfiles = channelProfiles; }
    public Map<String, String> getProfileAttributes() { return profileAttributes; }
    public void setProfileAttributes(Map<String, String> profileAttributes) { this.profileAttributes = profileAttributes; }
    public List<String> getInterestTags() { return interestTags; }
    public void setInterestTags(List<String> interestTags) { this.interestTags = interestTags; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}