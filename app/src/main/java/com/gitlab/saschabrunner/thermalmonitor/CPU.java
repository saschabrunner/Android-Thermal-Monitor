package com.gitlab.saschabrunner.thermalmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CPU {
    private File folder;

    private int lastFrequency;

    public CPU(File sysfsFolder) {
        if (isValidSysfsFolder(sysfsFolder)) {
            this.folder = sysfsFolder;
        } else {
            throw new IllegalArgumentException("Passed file object does not point to CPU in sysfs");
        }
    }

    private boolean isValidSysfsFolder(File sysfsFolder) {
        // Path must be "/sys/devices/system/cpu/cpu#" where # is one or more numbers
        return sysfsFolder
                .getAbsolutePath()
                .toLowerCase()
                .matches("(/sys/devices/system/cpu/cpu)[0-9]+$");
    }

    private String getCurFrequencyFilePath() {
        return folder.getAbsolutePath() + "/cpufreq/scaling_cur_freq";
    }

    public int getId() {
        String path = folder.getAbsolutePath();
        String cpuId = path.replace("/sys/devices/system/cpu/cpu", "");
        return Integer.parseInt(cpuId);
    }

    public void updateFrequency() throws IOException {
        // TODO: Keep reader open and just reset instead?
        try (BufferedReader reader = new BufferedReader(new FileReader(getCurFrequencyFilePath()))) {
            lastFrequency = Integer.valueOf(reader.readLine());
        }
    }

    public int getLastFrequency() {
        return lastFrequency;
    }
}
