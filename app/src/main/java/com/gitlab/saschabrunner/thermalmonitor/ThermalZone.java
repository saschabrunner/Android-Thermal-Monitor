package com.gitlab.saschabrunner.thermalmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ThermalZone {
    private File folder;
    private String type;

    private int lastTemperature;

    public ThermalZone(File sysfsFolder) throws IOException {
        if (isValidSysfsFolder(sysfsFolder)) {
            this.folder = sysfsFolder;
            this.type = readType();
        } else {
            throw new IllegalArgumentException("Passed file object does not point to thermal zone in sysfs");
        }
    }

    private boolean isValidSysfsFolder(File sysfsFolder) {
        // Path must be "/sys/class/thermal/thermal_zone#" where # is one or more numbers
        return sysfsFolder
                .getAbsolutePath()
                .toLowerCase()
                .matches("(/sys/class/thermal/thermal_zone)[0-9]+$");
    }

    private String readType() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(getTypeFilePath()))) {
            return reader.readLine();
        }
    }

    private String getTypeFilePath() {
        return folder.getAbsolutePath() + "/type";
    }

    private String getTemperatureFilePath() {
        return folder.getAbsolutePath() + "/temp";
    }

    public int getId() {
        String path = folder.getAbsolutePath();
        String thermalZoneId = path.replace("/sys/class/thermal/thermal_zone", "");
        return Integer.parseInt(thermalZoneId);
    }

    public void updateTemperature() throws IOException {
        // TODO: Keep reader open and just reset instead?
        try (BufferedReader reader = new BufferedReader(new FileReader(getTemperatureFilePath()))) {
            lastTemperature = Integer.valueOf(reader.readLine());
        }
    }

    public String getType() {
        return type;
    }

    public int getLastTemperature() {
        return lastTemperature;
    }

    @Override
    public String toString() {
        return "Zone " + this.getId() + " (" + this.getType() + "): " + this.getLastTemperature();
    }
}
