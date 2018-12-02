package com.gitlab.saschabrunner.thermalmonitor;

import android.util.Log;

import java.io.IOException;
import java.util.List;

public class ThermalMonitor implements Runnable {
    private static final String TAG = "ThermalMonitor";

    private MonitorService monitorService;
    private List<ThermalZone> thermalZones;

    public ThermalMonitor(MonitorService monitorService) {
        this.monitorService = monitorService;
        this.thermalZones = ThermalZone.getThermalZones();
    }

    @Override
    public void run() {
        while (monitorService.isMonitoringRunning()) {
            monitorService.awaitNotPaused();
            Log.v(TAG, "ThermalMonitor update");

            updateThermalZones();

            StringBuilder text = new StringBuilder();
            for (ThermalZone thermalZone : thermalZones) {
                text.append(thermalZone.toString()).append("\n");
            }
            monitorService.setNotificationText(text.toString(), 1);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (monitorService.isMonitoringRunning()) {
                    // No interrupt should happen except when monitor service quits
                    Log.e(TAG, "Unexcpected Interrupt received", e);
                    return;
                }
            }
        }
    }

    private void updateThermalZones() {
        for (ThermalZone thermalZone : thermalZones) {
            try {
                thermalZone.updateTemperature();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
