package com.gitlab.saschabrunner.thermalmonitor;

import java.io.IOException;
import java.util.List;

public class CPUFreqMonitor implements Runnable {
    private MonitorService monitorService;
    private List<CPU> cpus;

    public CPUFreqMonitor(MonitorService monitorService) {
        this.monitorService = monitorService;
        this.cpus = CPU.getCpus();
    }

    @Override
    public void run() {
        while (true) {
            updateCpus();

            StringBuilder text = new StringBuilder();
            for (CPU cpu : cpus) {
                text.append(cpu.toString()).append("\n");
            }
            monitorService.setNotificationText(text.toString(), 0);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void updateCpus() {
        for (CPU cpu : cpus) {
            try {
                cpu.update();
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        }
    }
}
