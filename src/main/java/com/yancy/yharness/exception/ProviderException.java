package com.yancy.yharness.exception;

public class ProviderException extends AgentException {
    public ProviderException(String message) {
        super("PROVIDER_ERROR", message);
    }

    public ProviderException(String message, Throwable cause) {
        super("PROVIDER_ERROR", message, cause);
    }
}