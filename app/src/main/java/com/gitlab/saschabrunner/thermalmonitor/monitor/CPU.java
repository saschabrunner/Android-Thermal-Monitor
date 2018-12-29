package com.gitlab.saschabrunner.thermalmonitor.monitor;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

public class CPU {
    private static final String TAG = "CPU";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_OFFLINE, STATE_ONLINE})
    public @interface STATE {
    }

    public static final int STATE_OFFLINE = 0;
    public static final int STATE_ONLINE = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FAILURE_REASON_OK, FAILURE_REASON_DIR_NOT_EXISTS, FAILURE_REASON_DIR_EMPTY,
            FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION})
    public @interface FAILURE_REASON {
    }

    public static final int FAILURE_REASON_OK = 0;
    public static final int FAILURE_REASON_DIR_NOT_EXISTS = 1;
    public static final int FAILURE_REASON_DIR_EMPTY = 2;
    public static final int FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION = 3;

    private File directory;
    private FileChannel stateFileChannel;

    private ByteBuffer buf = ByteBuffer.allocate(20);

    @STATE
    private int lastState;
    private int lastFrequency;

    private CPU(File sysfsDirectory) throws IOException {
        if (isValidSysfsDirectory(sysfsDirectory)) {
            this.directory = sysfsDirectory;
        } else {
            throw new IllegalArgumentException("Passed file object does not point to CPU in sysfs");
        }

        initStateFileChannel();
    }

    public void deinit() throws IOException {
        if (stateFileChannel != null) {
            stateFileChannel.close();
        }
    }

    private boolean isValidSysfsDirectory(File sysfsDirectory) {
        // Path must be "/sys/devices/system/cpu/cpu#" where # is one or more numbers
        return sysfsDirectory
                .getAbsolutePath()
                .toLowerCase()
                .matches("(/sys/devices/system/cpu/cpu)[0-9]+$");
    }

    private void initStateFileChannel() throws IOException {
        File onlineStateFile = new File(getOnlineStateFilePath());
        if (!onlineStateFile.exists()) {
            Log.w(TAG, "No online state file found, using alternative method");
            return;
        }

        Log.v(TAG, "Online state file found");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stateFileChannel = FileChannel.open(Paths.get(getOnlineStateFilePath()),
                    StandardOpenOption.READ);
        } else {
            FileInputStream stStream = new FileInputStream(getOnlineStateFilePath());
            stateFileChannel = stStream.getChannel();
        }
    }

    private String getOnlineStateFilePath() {
        return getOnlineStateFilePath(directory.getAbsolutePath());
    }

    private String getCurFrequencyFilePath() {
        return getCurFrequencyFilePath(directory.getAbsolutePath());
    }

    private void updateState() throws IOException {
        if (stateFileChannel != null) {
            // Reset file channel and buffer to beginning
            stateFileChannel.position(0);
            buf.position(0);

            // Read current frequency
            int length = stateFileChannel.read(buf);

            // Parse integer value
            String state = new String(buf.array(), 0, length - 1);

            // Determine state
            if ("0".equals(state)) {
                lastState = STATE_OFFLINE;
            } else {
                lastState = STATE_ONLINE;
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
            Log.v(TAG, "CPU is online");
            updateFrequency();
        }
    }

    public int getId() {
        String path = directory.getAbsolutePath();
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

    @NonNull
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

    @FAILURE_REASON
    public static int checkMonitoringAvailable() {
        // Check if dir is present
        File cpusDir = new File("/sys/devices/system/cpu");
        if (!cpusDir.exists()) {
            return FAILURE_REASON_DIR_NOT_EXISTS;
        }

        // Check if dirs for CPU cores are present
        File[] cpuDirs = filterCpus(cpusDir);
        if (cpuDirs == null || cpuDirs.length == 0) {
            return FAILURE_REASON_DIR_EMPTY;
        }

        // Check if scaling_cur_frequency is accessible for at least one core
        // The file might not exist for cores that are offline
        // We assume that at least one core is online
        boolean curFrequencyReadable = false;
        for (File cpuDir : cpuDirs) {
            File curFrequencyFile = new File(getCurFrequencyFilePath(cpuDir.getAbsolutePath()));
            if (curFrequencyFile.exists() && curFrequencyFile.canRead()) {
                curFrequencyReadable = true;
                break;
            }
        }

        if (!curFrequencyReadable) {
            return FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION;
        }

        return FAILURE_REASON_OK;
    }

    public static List<CPU> getCpus() {
        File cpusDir = new File("/sys/devices/system/cpu");
        File[] cpuDirs = filterCpus(cpusDir);

        List<CPU> cpus = new ArrayList<>(cpuDirs.length);
        for (File dir : cpuDirs) {
            try {
                cpus.add(new CPU(dir));
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        return cpus;
    }

    private static File[] filterCpus(File cpusDir) {
        return cpusDir.listFiles((dir, name) -> {
            String lcName = name.toLowerCase();
            return lcName.matches("(cpu)[0-9]+");
        });
    }

    private static String getOnlineStateFilePath(String cpuPath) {
        return cpuPath + "/online";
    }

    private static String getCurFrequencyFilePath(String cpuPath) {
        return cpuPath + "/cpufreq/scaling_cur_freq";
    }
}
