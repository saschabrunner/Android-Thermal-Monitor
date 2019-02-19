package com.gitlab.saschabrunner.thermalmonitor.main.monitor;

import android.content.Context;

import androidx.annotation.StringRes;

public class MonitorException extends Exception {
    @StringRes
    private int resourceId;

    public MonitorException(@StringRes int resourceId) {
        super();
        this.resourceId = resourceId;
    }

    public MonitorException(String message) {
        super(message);
        this.resourceId = -1;
    }

    /**
     * Use this method to get the translated message from the string-resources if available.
     * @param context Context used to access resources.
     * @return Message of the exception.
     */
    public String getMessage(Context context) {
        if (resourceId != -1) {
            return context.getString(resourceId);
        }

        return super.getMessage();
    }
}
