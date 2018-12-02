package com.gitlab.saschabrunner.thermalmonitor;

import android.util.Log;

import java.io.IOException;
import java.util.List;

public class CPUFreqMonitor implements Runnable {
    private static final String TAG = "CPUFreqMonitor";

    private final MonitorService monitorService;
    private final List<CPU> cpus;

    public CPUFreqMonitor(MonitorService monitorService) {
        this.monitorService = monitorService;
        this.cpus = CPU.getCpus();
    }

    @Override
    public void run() {
        while (monitorService.isMonitoringRunning()) {
            monitorService.awaitNotPaused();
            Log.v(TAG, "CPUFreqMonitor update");

            updateCpus();

            StringBuilder text = new StringBuilder();
            for (CPU cpu : cpus) {
                text.append(cpu.toString()).append("\n");
            }
            monitorService.setNotificationText(text.toString(), 0);

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
}
