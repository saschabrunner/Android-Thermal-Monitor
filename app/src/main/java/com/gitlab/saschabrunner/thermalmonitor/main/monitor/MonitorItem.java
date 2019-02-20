package com.gitlab.saschabrunner.thermalmonitor.main.monitor;

/**
 * One data item from a monitor (corresponds to one row in the overlay)
 */
public class MonitorItem {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
