package com.gitlab.saschabrunner.thermalmonitor.thermal;

public class ThermalZonePickerListItem {
    private static final int NO_ID = -1;

    private int recyclerViewId;

    private ThermalZoneInfo thermalZoneInfo;
    private String currentTemperature;

    private boolean selected;
    private boolean recommended;
    private boolean excluded;

    public ThermalZonePickerListItem(ThermalZoneMonitorItem item) {
        this(item.getThermalZoneInfo(), item.getValue());
    }

    public ThermalZonePickerListItem(ThermalZoneInfo thermalZoneInfo, String currentTemperature) {
        this.recyclerViewId = NO_ID;
        this.thermalZoneInfo = thermalZoneInfo;
        this.currentTemperature = currentTemperature;
    }

    public int getRecyclerViewId() {
        return recyclerViewId;
    }

    public void setRecyclerViewId(int recyclerViewId) {
        this.recyclerViewId = recyclerViewId;
    }

    public ThermalZoneInfo getThermalZoneInfo() {
        return thermalZoneInfo;
    }

    public void setThermalZoneInfo(ThermalZoneInfo thermalZoneInfo) {
        this.thermalZoneInfo = thermalZoneInfo;
    }

    public String getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }
}
