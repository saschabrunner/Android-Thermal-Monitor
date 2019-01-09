package com.gitlab.saschabrunner.thermalmonitor.monitor;

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

    private String type;

    private FileChannel temperatureFileChannel;
    private ByteBuffer buf = ByteBuffer.allocate(10);

    private int lastTemperature;

    public ThermalZone(File sysfsDirectory) throws IOException {
        super(sysfsDirectory);

        this.type = readType();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            temperatureFileChannel = FileChannel.open(Paths.get(getTemperatureFilePath()), StandardOpenOption.READ);
        } else {
            FileInputStream stream = new FileInputStream(getTemperatureFilePath());
            temperatureFileChannel = stream.getChannel();
        }
    }

    private String readType() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(getTypeFilePath()))) {
            return reader.readLine();
        }
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
            // Reset file channel and buffer to beginning
            temperatureFileChannel.position(0);
            buf.position(0);

            // Read current temperature
            int length = temperatureFileChannel.read(buf);

            // Parse integer value
            lastTemperature = Integer.valueOf(new String(buf.array(), 0, length - 1));
        } catch (IOException e) {
            Log.e(TAG, "Couldn't update temperature of thermal zone '" + type + "'", e);
            lastTemperature = -1;
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getLastTemperature() {
        return lastTemperature;
    }
}
