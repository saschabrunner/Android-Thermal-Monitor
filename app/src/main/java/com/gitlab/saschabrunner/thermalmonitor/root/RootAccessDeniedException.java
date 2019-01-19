package com.gitlab.saschabrunner.thermalmonitor.root;

public class RootAccessDeniedException extends RootAccessException {
    public RootAccessDeniedException() {
        super("Root access has been denied");
    }
}
