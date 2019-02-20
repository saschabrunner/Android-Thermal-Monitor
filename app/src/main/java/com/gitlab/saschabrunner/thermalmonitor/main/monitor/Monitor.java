package com.gitlab.saschabrunner.thermalmonitor.main.monitor;

import android.content.SharedPreferences;

public interface Monitor {
    /**
     *
     * @param monitorPreferences
     * @return 0 to indicate support, non zero value to indicate no support
     */
    int checkSupported(SharedPreferences monitorPreferences) throws MonitorException;

    /**
     *
     * @param controller
     * @param monitorPreferences
     */
    void init(MonitorController controller, SharedPreferences monitorPreferences)
            throws MonitorException;

    /**
     *
     */
    void deinit();
}
