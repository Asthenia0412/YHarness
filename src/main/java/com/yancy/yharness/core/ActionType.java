
package com.yancy.yharness.core;

public enum ActionType {
    TOOL_CALL("tool_call"),
    FINISH("finish"),
    MESSAGE("message");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ActionType fromValue(String value) {
        for (ActionType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return MESSAGE;
    }
}
