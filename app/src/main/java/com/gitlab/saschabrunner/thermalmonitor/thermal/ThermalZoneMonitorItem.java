package com.gitlab.saschabrunner.thermalmonitor.thermal;

import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorItem;

public class ThermalZoneMonitorItem extends MonitorItem {
    private ThermalZoneInfo thermalZoneInfo;

    public ThermalZoneMonitorItem(ThermalZoneInfo thermalZoneInfo) {
        super();
        this.thermalZoneInfo = thermalZoneInfo;
    }

    public ThermalZoneInfo getThermalZoneInfo() {
        return thermalZoneInfo;
    }

    public void setThermalZoneInfo(ThermalZoneInfo thermalZoneInfo) {
        this.thermalZoneInfo = thermalZoneInfo;
    }
}
