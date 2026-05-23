package com.yancy.yharness.exception;

public class AgentException extends RuntimeException {
    private final String code;

    public AgentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AgentException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() { return code; }
}