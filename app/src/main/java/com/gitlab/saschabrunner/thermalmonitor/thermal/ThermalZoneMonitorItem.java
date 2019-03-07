package com.gitlab.saschabrunner.thermalmonitor.thermal;

import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorItem;

public class ThermalZoneMonitorItem extends MonitorItem {
    private static final int NO_TEMPERATURE = -1;

    private ThermalZoneInfo thermalZoneInfo;
    private int lastTemperature = -1; // Temperature in degree celsius

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

    /**
     * Get the last temperature.
     *
     * @return Last recorded temperature in degree celsius.
     */
    public int getLastTemperature() {
        return lastTemperature;
    }

    public void setLastTemperature(int lastTemperature) {
        this.lastTemperature = lastTemperature;
    }
}
