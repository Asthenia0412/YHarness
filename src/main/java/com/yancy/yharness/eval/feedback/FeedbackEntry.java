package com.yancy.yharness.eval.feedback;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FeedbackEntry {
    private String id;
    private String taskId;
    private String userId;
    private String agentSessionId;
    private int rating;
    private String comment;
    private String category;
    private Map<String, Object> metadata;
    private FeedbackSource source;
    private LocalDateTime createdAt;
    private boolean incorporated;

    public enum FeedbackSource {
        CUSTOMER_SERVICE_REVIEW,
        USER_REPORT,
        A_DETECTED,
        MANUAL_AUDIT
    }
}