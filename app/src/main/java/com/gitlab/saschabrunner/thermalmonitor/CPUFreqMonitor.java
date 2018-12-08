package com.gitlab.saschabrunner.thermalmonitor;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CPUFreqMonitor implements Runnable, Monitor {
    private static final String TAG = "CPUFreqMonitor";

    private final MonitorService monitorService;
    private final List<CPU> cpus;
    private Map<CPU, OverlayListItem> listItemByCpu;

    public CPUFreqMonitor(MonitorService monitorService) {
        this.monitorService = monitorService;
        this.cpus = CPU.getCpus();
        this.listItemByCpu = new HashMap<>();
        for (CPU cpu : cpus) {
            OverlayListItem listItem = new OverlayListItem();
            listItem.setLabel("CPU" + cpu.getId());
            listItemByCpu.put(cpu, listItem);
            monitorService.addListItem(listItem);
        }
    }

    public void deinit() {
        for (CPU cpu : cpus) {
            try {
                cpu.deinit();
            } catch (IOException e) {
                Log.e(TAG, "Couldn't deinit CPU", e);
            }
        }
    }

    @Override
    public void run() {
        while (monitorService.isMonitoringRunning()) {
            monitorService.awaitNotPaused();
            Log.v(TAG, "CPUFreqMonitor update");

            updateCpus();

            for (CPU cpu : cpus) {
                OverlayListItem listItem = listItemByCpu.get(cpu);
                assert listItem != null;
                listItem.setValue(buildCpuValueString(cpu));
                monitorService.updateListItem(listItem);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                if (monitorService.isMonitoringRunning()) {
                    // No interrupt should happen except when monitor service quits
                    Log.e(TAG, "Unexcpected Interrupt received", e);
                    return;
                }
            }
        }
    }

    private void updateCpus() {
        for (CPU cpu : cpus) {
            try {
                cpu.update();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private String buildCpuValueString(CPU cpu) {
        if (cpu.getLastState() == CPU.STATE_OFFLINE) {
            return monitorService.getString(R.string.offline);
        } else {
            return String.format(Locale.getDefault(), "%6.1f MHz",
                    (double) cpu.getLastFrequency() / 1000);
        }
    }
}
