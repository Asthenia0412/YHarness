package com.yancy.yharness.eval.feedback;

import com.yancy.yharness.eval.model.EvalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class FeedbackService {
    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);
    private final Map<String, FeedbackEntry> feedbackStore = new ConcurrentHashMap<>();

    public FeedbackEntry submitFeedback(FeedbackEntry entry) {
        if (entry.getId() == null) {
            entry.setId(UUID.randomUUID().toString());
        }
        if (entry.getCreatedAt() == null) {
            entry.setCreatedAt(java.time.LocalDateTime.now());
        }
        entry.setIncorporated(false);
        feedbackStore.put(entry.getId(), entry);
        log.info("[Feedback] Received from {}: rating={}, category={}, taskId={}",
                entry.getSource(), entry.getRating(), entry.getCategory(), entry.getTaskId());
        return entry;
    }

    public List<FeedbackEntry> getFeedbackForTask(String taskId) {
        return feedbackStore.values().stream()
                .filter(f -> taskId.equals(f.getTaskId()))
                .sorted(Comparator.comparing(FeedbackEntry::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<FeedbackEntry> getUnincorporatedFeedback() {
        return feedbackStore.values().stream()
                .filter(f -> !f.isIncorporated())
                .collect(Collectors.toList());
    }

    public List<EvalTask> enrichEvalSetFromFeedback() {
        List<EvalTask> newTasks = new ArrayList<>();
        for (FeedbackEntry entry : getUnincorporatedFeedback()) {
            if (entry.getRating() <= 2) {
                EvalTask task = new EvalTask();
                task.setId("feedback_task_" + entry.getId());
                task.setName("Feedback: " + entry.getCategory() + " - " + entry.getComment().substring(0, Math.min(30, entry.getComment().length())));
                task.setDescription(entry.getComment());
                task.setCategory(EvalTask.EvalCategory.CAPABILITY);
                task.setOwner("feedback-system");
                task.setSource("FEEDBACK_" + entry.getSource());
                newTasks.add(task);
                entry.setIncorporated(true);
                log.info("[Feedback] Enriched eval set with task: {} from feedback: {}",
                        task.getId(), entry.getId());
            }
        }
        return newTasks;
    }
}