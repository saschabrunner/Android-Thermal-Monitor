package com.gitlab.saschabrunner.thermalmonitor.thermal;

/**
 * Basic info about a thermal zone (immutable)
 */
public class ThermalZoneInfo {
    private final String dir;
    private final int id;
    private final String type;

    public ThermalZoneInfo(String dir, int id, String type) {
        this.dir = dir;
        this.id = id;
        this.type = type;
    }

    public String getDir() {
        return dir;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
