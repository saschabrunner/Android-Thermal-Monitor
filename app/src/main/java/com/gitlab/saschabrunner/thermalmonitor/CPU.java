package com.gitlab.saschabrunner.thermalmonitor;

import android.support.annotation.IntDef;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CPU {
    private static final String TAG = "CPU";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_OFFLINE, STATE_ONLINE})
    public @interface STATE {
    }

    public static final int STATE_OFFLINE = 0;
    public static final int STATE_ONLINE = 1;

    private File folder;

    @STATE
    private int lastState;
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

    private String getOnlineStateFilePath() {
        return folder.getAbsolutePath() + "/online";
    }

    private String getCurFrequencyFilePath() {
        return folder.getAbsolutePath() + "/cpufreq/scaling_cur_freq";
    }

    private void updateState() throws IOException {
        File onlineStateFile = new File(getOnlineStateFilePath());
        if (onlineStateFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(onlineStateFile))) {
                String state = reader.readLine();
                if ("0".equals(state)) {
                    lastState = STATE_OFFLINE;
                } else {
                    lastState = STATE_ONLINE;
                }
            }
        } else {
            File curFrequencyFile = new File(getCurFrequencyFilePath());
            if (curFrequencyFile.exists()) {
                lastState = STATE_ONLINE;
            } else {
                lastState = STATE_OFFLINE;
            }
        }
    }

    private void updateFrequency() throws IOException {
        // TODO: Keep reader open and just reset instead?
        try (BufferedReader reader = new BufferedReader(new FileReader(getCurFrequencyFilePath()))) {
            lastFrequency = Integer.valueOf(reader.readLine());
        } catch (FileNotFoundException e) {
            // There's a slim chance, that the CPU went offline since we last checked
            Log.d(TAG, "CPU went offline since last check");
        }
    }

    public void update() throws IOException {
        updateState();

        if (lastState != STATE_OFFLINE) {
            updateFrequency();
        }
    }

    public int getId() {
        String path = folder.getAbsolutePath();
        String cpuId = path.replace("/sys/devices/system/cpu/cpu", "");
        return Integer.parseInt(cpuId);
    }

    @STATE
    public int getLastState() {
        return lastState;
    }

    public int getLastFrequency() {
        return lastFrequency;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("CPU")
                .append(this.getId())
                .append(": ");

        if (STATE_ONLINE == this.lastState) {
            out.append(this.getLastFrequency())
                    .append("KHz");
        } else {
            out.append("offline");
        }

        return out.toString();
    }
}
