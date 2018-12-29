package com.gitlab.saschabrunner.thermalmonitor.monitor;

import com.gitlab.saschabrunner.thermalmonitor.MonitorService;

public interface Monitor {
    /**
     *
     * @return 0 to indicate support, non zero value to indicate no support
     */
    int checkSupported();

    /**
     *
     * @param monitorService
     */
    void init(MonitorService monitorService);

    /**
     *
     */
    void deinit();
}
