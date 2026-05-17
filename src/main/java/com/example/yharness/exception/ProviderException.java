
package com.example.yharness.exception;

public class ProviderException extends AgentException {
    
    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
