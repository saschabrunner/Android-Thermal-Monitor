package com.gitlab.saschabrunner.thermalmonitor.root;

public class RootAccessDisabledException extends RootAccessException {
    public RootAccessDisabledException() {
        super("Root access has been globally disabled in the app preferences");
    }
}
