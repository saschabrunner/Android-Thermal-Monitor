package com.gitlab.saschabrunner.thermalmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.IntDef;

public class ThermalZone {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FAILURE_REASON_OK, FAILURE_REASON_DIR_NOT_EXISTS, FAILURE_REASON_NO_THERMAL_ZONES,
            FAILURE_REASON_TYPE_NO_PERMISSION, FAILURE_REASON_TEMP_NO_PERMISSION})
    public @interface FAILURE_REASON {
    }

    public static final int FAILURE_REASON_OK = 0;
    public static final int FAILURE_REASON_DIR_NOT_EXISTS = 1;
    public static final int FAILURE_REASON_NO_THERMAL_ZONES = 2;
    public static final int FAILURE_REASON_TYPE_NO_PERMISSION = 3;
    public static final int FAILURE_REASON_TEMP_NO_PERMISSION = 4;

    private File directory;
    private String type;

    private int lastTemperature;

    private ThermalZone(File sysfsDirectory) throws IOException {
        if (isValidSysfsDirectory(sysfsDirectory)) {
            this.directory = sysfsDirectory;
            this.type = readType();
        } else {
            throw new IllegalArgumentException("Passed file object does not point to thermal zone in sysfs");
        }
    }

    private boolean isValidSysfsDirectory(File sysfsDirectory) {
        // Path must be "/sys/class/thermal/thermal_zone#" where # is one or more numbers
        return sysfsDirectory
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
        return getTypeFilePath(directory.getAbsolutePath());
    }

    private String getTemperatureFilePath() {
        return getTemperatureFilePath(directory.getAbsolutePath());
    }

    public int getId() {
        String path = directory.getAbsolutePath();
        String thermalZoneId = path.replace("/sys/class/thermal/thermal_zone", "");
        return Integer.parseInt(thermalZoneId);
    }

    public void updateTemperature() throws IOException {
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

    @FAILURE_REASON
    public static int checkMonitoringAvailable() {
        // Check if dir is present
        File thermalZonesDir = new File("/sys/class/thermal");
        if (!thermalZonesDir.exists()) {
            return FAILURE_REASON_DIR_NOT_EXISTS;
        }

        // Check if dirs for thermal zones are present
        File[] thermalZoneDirs = filterThermalZones(thermalZonesDir);
        if (thermalZoneDirs == null || thermalZoneDirs.length == 0) {
            return FAILURE_REASON_NO_THERMAL_ZONES;
        }

        // Check if required files are accessible for every thermal zone
        // Right now we assume same permissions for all thermal zones
        for (File zoneDir : thermalZoneDirs) {
            File typeFile = new File(getTypeFilePath(zoneDir.getAbsolutePath()));
            if (!typeFile.canRead()) {
                return FAILURE_REASON_TYPE_NO_PERMISSION;
            }

            File tempFile = new File(getTemperatureFilePath(zoneDir.getAbsolutePath()));
            if (!tempFile.canRead()) {
                return FAILURE_REASON_TEMP_NO_PERMISSION;
            }
        }

        return FAILURE_REASON_OK;
    }

    public static List<ThermalZone> getThermalZones() {
        File thermalZonesDir = new File("/sys/class/thermal");
        File[] thermalZoneDirs = filterThermalZones(thermalZonesDir);

        List<ThermalZone> thermalZones = new ArrayList<>(thermalZoneDirs.length);
        for (File dir : thermalZoneDirs) {
            try {
                thermalZones.add(new ThermalZone(dir));
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        return thermalZones;
    }

    private static File[] filterThermalZones(File thermalZonesDir) {
        return thermalZonesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lcName = name.toLowerCase();
                return lcName.matches("(thermal_zone)[0-9]+");
            }
        });
    }

    private static String getTypeFilePath(String thermalZonePath) {
        return thermalZonePath + "/type";
    }

    private static String getTemperatureFilePath(String thermalZonePath) {
        return thermalZonePath + "/temp";
    }
}
