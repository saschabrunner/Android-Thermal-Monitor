package com.gitlab.saschabrunner.thermalmonitor.monitor;

import android.content.SharedPreferences;

import com.gitlab.saschabrunner.thermalmonitor.MonitorService;

public interface Monitor {
    /**
     *
     * @param monitorPreferences
     * @return 0 to indicate support, non zero value to indicate no support
     */
    int checkSupported(SharedPreferences monitorPreferences) throws MonitorException;

    /**
     *
     * @param monitorService
     * @param monitorPreferences
     */
    void init(MonitorService monitorService, SharedPreferences monitorPreferences) throws MonitorException;

    /**
     *
     */
    void deinit();
}
