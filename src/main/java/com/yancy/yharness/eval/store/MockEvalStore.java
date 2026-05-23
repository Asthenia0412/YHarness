package com.yancy.yharness.eval.store;

import com.yancy.yharness.eval.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class MockEvalStore {
    private final Map<String, EvalTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, EvalSuite> suites = new ConcurrentHashMap<>();
    private final Map<String, EvalTrial> trials = new ConcurrentHashMap<>();
    private final Map<String, List<String>> suiteTasks = new ConcurrentHashMap<>();

    public void saveTask(EvalTask task) {
        tasks.put(task.getId(), task);
    }

    public EvalTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    public List<EvalTask> listTasks() {
        return tasks.values().stream()
                .sorted(Comparator.comparing(EvalTask::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<EvalTask> getTasksBySuite(String suiteId) {
        List<String> taskIds = suiteTasks.getOrDefault(suiteId, List.of());
        return taskIds.stream()
                .map(tasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void saveSuite(EvalSuite suite) {
        suites.put(suite.getId(), suite);
        if (suite.getTaskIds() != null) {
            suiteTasks.put(suite.getId(), new ArrayList<>(suite.getTaskIds()));
        }
    }

    public EvalSuite getSuite(String suiteId) {
        return suites.get(suiteId);
    }

    public List<EvalSuite> listSuites() {
        return suites.values().stream()
                .sorted(Comparator.comparing(EvalSuite::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public void saveTrial(EvalTrial trial) {
        trials.put(trial.getId(), trial);
    }

    public EvalTrial getTrial(String trialId) {
        return trials.get(trialId);
    }

    public List<EvalTrial> getTrialsByTask(String taskId) {
        return trials.values().stream()
                .filter(t -> taskId.equals(t.getTaskId()))
                .sorted(Comparator.comparing(EvalTrial::getExecutedAt))
                .collect(Collectors.toList());
    }

    public void deleteTask(String taskId) {
        tasks.remove(taskId);
    }

    public void deleteSuite(String suiteId) {
        suites.remove(suiteId);
        suiteTasks.remove(suiteId);
    }
}