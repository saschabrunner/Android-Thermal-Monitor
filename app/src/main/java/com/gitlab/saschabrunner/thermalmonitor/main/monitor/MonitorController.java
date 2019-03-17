package com.gitlab.saschabrunner.thermalmonitor.main.monitor;

/**
 * Controller for a Monitor
 * <p>
 * A monitor talks to one controller to determine whether it should pause and to deliver updates.
 */
public interface MonitorController {
    void addItem(MonitorItem item);

    void updateItem(MonitorItem item);

    boolean isRunning();

    void awaitNotPaused();
}
