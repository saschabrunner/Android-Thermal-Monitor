package com.gitlab.saschabrunner.thermalmonitor.monitor;

import java.io.File;

import androidx.annotation.NonNull;

public abstract class ThermalZoneBase {
    private File directory;

    public ThermalZoneBase(File sysfsDirectory) {
        if (isValidSysfsDirectory(sysfsDirectory)) {
            this.directory = sysfsDirectory;
        } else {
            throw new IllegalArgumentException("Passed file object does not point to thermal zone in sysfs");
        }
    }

    public abstract void deinit();

    public abstract void updateTemperature();

    public abstract String getType();

    public abstract int getLastTemperature();

    private boolean isValidSysfsDirectory(File sysfsDirectory) {
        // Path must be "/sys/class/thermal/thermal_zone#" where # is one or more numbers
        return sysfsDirectory
                .getAbsolutePath()
                .toLowerCase()
                .matches("(/sys/class/thermal/thermal_zone)[0-9]+$");
    }

    protected String getTypeFilePath() {
        return getTypeFilePath(directory.getAbsolutePath());
    }

    protected String getTemperatureFilePath() {
        return getTemperatureFilePath(directory.getAbsolutePath());
    }

    public int getId() {
        String path = directory.getAbsolutePath();
        String thermalZoneId = path.replace("/sys/class/thermal/thermal_zone", "");
        return Integer.parseInt(thermalZoneId);
    }

    @NonNull
    @Override
    public String toString() {
        return "Zone " + this.getId() + " (" + this.getType() + "): " + this.getLastTemperature();
    }

    public static String getTypeFilePath(String thermalZonePath) {
        return thermalZonePath + "/type";
    }

    public static String getTemperatureFilePath(String thermalZonePath) {
        return thermalZonePath + "/temp";
    }
}
