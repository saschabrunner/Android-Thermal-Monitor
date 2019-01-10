package com.gitlab.saschabrunner.thermalmonitor.monitor;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import androidx.annotation.IntDef;

public class CPU {
    private static final String TAG = "CPU";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_OFFLINE, STATE_ONLINE})
    public @interface STATE {
    }

    public static final int STATE_OFFLINE = 0;
    public static final int STATE_ONLINE = 1;

    private File directory;
    private FileChannel stateFileChannel;

    private ByteBuffer buf = ByteBuffer.allocate(20);

    @STATE
    private int lastState;
    private int lastFrequency;

    public CPU(File sysfsDirectory) throws IOException {
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

    private void updateFrequency() {
        try (BufferedReader reader = new BufferedReader(new FileReader(getCurFrequencyFilePath()))) {
            lastFrequency = Integer.valueOf(reader.readLine());
        } catch (IOException e) {
            // There's a slim chance, that the CPU went offline since we last checked
            Log.d(TAG, "CPU went offline since last check", e);
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

    public static String getOnlineStateFilePath(String cpuPath) {
        return cpuPath + "/online";
    }

    public static String getCurFrequencyFilePath(String cpuPath) {
        return cpuPath + "/cpufreq/scaling_cur_freq";
    }
}
