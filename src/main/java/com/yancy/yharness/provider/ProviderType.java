
package com.yancy.yharness.provider;

public enum ProviderType {
    OPENAI("OPENAI"),
    ANTHROPIC("ANTHROPIC");

    private final String value;

    ProviderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProviderType fromValue(String value) {
        for (ProviderType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown provider type: " + value);
    }
}
