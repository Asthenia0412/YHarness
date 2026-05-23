package com.yancy.yharness.eval.isolation;

public class EvalContext {
    private static final ThreadLocal<Boolean> evalMode = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<String> evalUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> evalSessionId = new ThreadLocal<>();

    public static void enter(String userId) {
        evalMode.set(true);
        evalUserId.set("eval_" + userId);
        evalSessionId.set("eval_session_" + System.currentTimeMillis());
    }

    public static void exit() {
        evalMode.remove();
        evalUserId.remove();
        evalSessionId.remove();
    }

    public static boolean isEvalMode() {
        return evalMode.get();
    }

    public static String getEvalUserId() {
        return evalUserId.get();
    }

    public static String getEvalSessionId() {
        return evalSessionId.get();
    }
}