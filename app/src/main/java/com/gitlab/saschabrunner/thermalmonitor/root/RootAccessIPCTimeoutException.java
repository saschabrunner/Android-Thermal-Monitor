package com.gitlab.saschabrunner.thermalmonitor.root;

public class RootAccessIPCTimeoutException extends RootAccessException {
    public RootAccessIPCTimeoutException() {
        super("Timed out waiting for Root IPC");
    }
}
