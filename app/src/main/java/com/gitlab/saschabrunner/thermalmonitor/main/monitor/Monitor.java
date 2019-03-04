package com.gitlab.saschabrunner.thermalmonitor.main.monitor;

public interface Monitor {
    /**
     *
     * @param preferences
     * @return 0 to indicate support, non zero value to indicate no support
     */
    int checkSupported(MonitorPreferences preferences) throws MonitorException;

    /**
     *
     * @param controller
     * @param preferences
     */
    void init(MonitorController controller, MonitorPreferences preferences)
            throws MonitorException;

    /**
     *
     */
    void deinit();
}
