package com.gitlab.saschabrunner.thermalmonitor;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThermalMonitor implements Runnable, Monitor {
    private static final String TAG = "ThermalMonitor";

    private final MonitorService monitorService;
    private final List<ThermalZone> thermalZones;
    private Map<ThermalZone, OverlayListItem> listItemByThermalZone;

    public ThermalMonitor(MonitorService monitorService) {
        this.monitorService = monitorService;
        this.thermalZones = ThermalZone.getThermalZones();
        this.listItemByThermalZone = new HashMap<>();
        for (ThermalZone thermalZone : thermalZones) {
            OverlayListItem listItem = new OverlayListItem();
            listItem.setLabel(thermalZone.getType());
            listItemByThermalZone.put(thermalZone, listItem);
            monitorService.addListItem(listItem);
        }
    }

    public void deinit() {
        for (ThermalZone thermalZone : thermalZones) {
            try {
                thermalZone.deinit();
            } catch (IOException e) {
                Log.e(TAG, "Couldn't deinit thermal zone", e);
            }
        }
    }

    @Override
    public void run() {
        while (monitorService.isMonitoringRunning()) {
            monitorService.awaitNotPaused();
            Log.v(TAG, "ThermalMonitor update");

            updateThermalZones();

            for (ThermalZone thermalZone : thermalZones) {
                OverlayListItem listItem = listItemByThermalZone.get(thermalZone);
                assert listItem != null;
                listItem.setValue(String.valueOf(thermalZone.getLastTemperature()));
                monitorService.updateListItem(listItem);
            }

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
