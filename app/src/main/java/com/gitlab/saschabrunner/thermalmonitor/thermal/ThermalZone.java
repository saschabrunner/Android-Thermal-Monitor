package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ThermalZone extends ThermalZoneBase {
    private static final String TAG = "ThermalZone";

    private FileChannel temperatureFileChannel;
    private ByteBuffer buf = ByteBuffer.allocate(10);

    private int lastTemperature;
    private int factor; // Factor needed to convert sensor value to degree celsius

    public ThermalZone(File sysfsDirectory) throws IOException {
        super(sysfsDirectory.getAbsolutePath());
        setType(readType());

        openTemperatureFile();
        this.factor = detectFactor(readRawTemperature());
    }

    private String readType() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(getTypeFilePath()))) {
            return reader.readLine();
        }
    }

    private void openTemperatureFile() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            temperatureFileChannel = FileChannel.open(Paths.get(getTemperatureFilePath()), StandardOpenOption.READ);
        } else {
            FileInputStream stream = new FileInputStream(getTemperatureFilePath());
            temperatureFileChannel = stream.getChannel();
        }
    }

    private int readRawTemperature() throws IOException {
        // Reset file channel and buffer to beginning
        temperatureFileChannel.position(0);
        buf.position(0);

        // Read current temperature
        int length = temperatureFileChannel.read(buf);

        // Parse integer value
        return Integer.valueOf(new String(buf.array(), 0, length - 1));
    }

    @Override
    public void deinit() {
        try {
            temperatureFileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTemperature() {
        try {
            lastTemperature = readRawTemperature() / factor;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't update temperature of thermal zone "
                    + getInfo().getId() + " (" + getInfo().getType() + ")", e);
            lastTemperature = -1;
        }
    }

    @Override
    public int getLastTemperature() {
        return lastTemperature;
    }
}
