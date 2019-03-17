package com.gitlab.saschabrunner.thermalmonitor.root;

public abstract class RootAccessException extends Exception {
    public RootAccessException(String message) {
        super(message);
    }

    public RootAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
