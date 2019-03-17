package com.gitlab.saschabrunner.thermalmonitor.main.monitor;

import androidx.annotation.StringRes;

public class MonitorException extends Exception {
    @StringRes
    private final int resourceId;

    public MonitorException(@StringRes int resourceId) {
        super();
        this.resourceId = resourceId;
    }

    @StringRes
    public int getResourceId() {
        return resourceId;
    }
}
