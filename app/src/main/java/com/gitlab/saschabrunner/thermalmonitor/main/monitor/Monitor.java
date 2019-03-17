package com.gitlab.saschabrunner.thermalmonitor.main.monitor;

public interface Monitor {
    /**
     * Checks whether the monitor is operational with the supplied preferences.
     * @param preferences Preferences to check compatibility with.
     * @return 0 to indicate support, non zero value to indicate no support.
     */
    int checkSupported(MonitorPreferences preferences);

    /**
     * Initializes the monitor and assigns a monitor controller to it.
     * @param controller Monitor controller that is called by the monitor to check whether
     *                   it should pause/stop.
     * @param preferences Preferences to use for the monitor.
     */
    void init(MonitorController controller, MonitorPreferences preferences)
            throws MonitorException;

    /**
     * Deinitializes a monitor to close handles / free up resources.
     */
    void deinit();
}
