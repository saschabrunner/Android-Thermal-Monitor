package com.gitlab.saschabrunner.thermalmonitor.thermal;

public abstract class ThermalZoneBase {
    private ThermalZoneInfo info;

    public ThermalZoneBase(String thermalZonePath) {
        this(thermalZonePath, "undefined");
    }

    public ThermalZoneBase(String thermalZonePath, String type) {
        this.info = new ThermalZoneInfo(thermalZonePath, getId(thermalZonePath), type);
        if (!isValidSysfsDirectory()) {
            throw new IllegalArgumentException("Passed directory does not point to " +
                    "thermal zone in sysfs");
        }
    }

    public abstract void deinit();

    public abstract void updateTemperature();

    public abstract int getLastTemperature();

    public ThermalZoneInfo getInfo() {
        return info;
    }

    private boolean isValidSysfsDirectory() {
        // Path must be "/sys/class/thermal/thermal_zone#" where # is one or more numbers
        return info.getDir()
                .toLowerCase()
                .matches("(/sys/class/thermal/thermal_zone)[0-9]+$");
    }

    protected String getTypeFilePath() {
        return getTypeFilePath(info.getDir());
    }

    protected String getTemperatureFilePath() {
        return getTemperatureFilePath(info.getDir());
    }

    protected void setType(String type) {
        this.info = new ThermalZoneInfo(info.getDir(), info.getId(), type);
    }

    protected static int detectFactor(int rawTemperature) {
        if (rawTemperature > 10000) {
            return 1000;
        } else if (rawTemperature > 1000) {
            return 100;
        } else if (rawTemperature > 100) {
            return 10;
        } else {
            return 1;
        }
    }

    public static String getTypeFilePath(String thermalZonePath) {
        return thermalZonePath + "/type";
    }

    public static String getTemperatureFilePath(String thermalZonePath) {
        return thermalZonePath + "/temp";
    }

    public static int getId(String thermalZonePath) {
        String thermalZoneId = thermalZonePath.replace(
                "/sys/class/thermal/thermal_zone", "");
        return Integer.parseInt(thermalZoneId);
    }
}
