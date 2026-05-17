
package com.yancy.yharness.hooks;

public enum HookType {
    ON_AGENT_START("onAgentStart"),
    ON_AGENT_END("onAgentEnd"),
    ON_CONTEXT_INIT("onContextInit"),
    ON_CONTEXT_UPDATE("onContextUpdate"),
    ON_MESSAGE_RECEIVED("onMessageReceived"),
    ON_MESSAGE_SEND("onMessageSend"),
    ON_TOOL_CALL("onToolCall"),
    ON_TOOL_RESULT("onToolResult"),
    ON_PROVIDER_CALL("onProviderCall"),
    ON_PROVIDER_RESPONSE("onProviderResponse"),
    ON_ERROR("onError"),
    ON_REACT_START("onReActStart"),
    ON_REACT_END("onReActEnd"),
    ON_THOUGHT_GENERATED("onThoughtGenerated");

    private final String value;

    HookType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static HookType fromValue(String value) {
        for (HookType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown hook type: " + value);
    }
}
