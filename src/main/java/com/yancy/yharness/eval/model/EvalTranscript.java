package com.yancy.yharness.eval.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EvalTranscript {
    private String trialId;
    private String systemPrompt;
    private List<TranscriptEntry> entries;
    private Map<String, Object> finalState;

    @Data
    public static class TranscriptEntry {
        private String role;
        private String content;
        private String toolName;
        private Map<String, Object> toolArguments;
        private String toolResult;
        private long timestampMs;
    }
}