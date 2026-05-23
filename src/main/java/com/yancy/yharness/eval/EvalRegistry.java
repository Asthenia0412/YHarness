package com.yancy.yharness.eval;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EvalRegistry {
    private final Map<String, EvalTarget> targets = new ConcurrentHashMap<>();

    public void register(EvalTarget target) {
        targets.put(target.id(), target);
    }

    public EvalTarget findById(String id) {
        EvalTarget target = targets.get(id);
        if (target == null) {
            throw new IllegalArgumentException("EvalTarget not found: " + id);
        }
        return target;
    }

    public List<Map.Entry<String, String>> listAll() {
        return targets.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().name()))
                .collect(Collectors.toList());
    }

    public boolean contains(String id) {
        return targets.containsKey(id);
    }
}