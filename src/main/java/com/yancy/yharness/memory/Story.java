package com.yancy.yharness.memory;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Story {
    private String userId;
    private String leadStage;
    private String language;
    private LocalDateTime lastContactAt;
    private int totalConversations;
    private List<Conversation.SessionSummary> recentConversations = new ArrayList<>();
    private Map<String, String> channelProfiles = new HashMap<>();
    private Map<String, String> profileAttributes = new HashMap<>();
    private List<String> interestTags = new ArrayList<>();
    private int version;
    private LocalDateTime updatedAt;
}