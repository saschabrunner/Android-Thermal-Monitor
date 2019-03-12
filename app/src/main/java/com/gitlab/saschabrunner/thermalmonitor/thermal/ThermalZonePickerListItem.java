package com.gitlab.saschabrunner.thermalmonitor.thermal;

public class ThermalZonePickerListItem {
    private static final int NO_ID = -1;

    private int recyclerViewId;

    private ThermalZoneInfo thermalZoneInfo;
    private int currentTemperatureCelsius;
    private String currentTemperatureUiValue;

    private boolean selected;
    private boolean recommended;
    private boolean excluded;

    public ThermalZonePickerListItem(ThermalZoneMonitorItem item) {
        this(item.getThermalZoneInfo(), item.getLastTemperature());
    }

    public ThermalZonePickerListItem(ThermalZoneInfo thermalZoneInfo, int currentTemperatureCelsius) {
        this.recyclerViewId = NO_ID;
        this.thermalZoneInfo = thermalZoneInfo;
        this.currentTemperatureCelsius = currentTemperatureCelsius;
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

    public int getCurrentTemperatureCelsius() {
        return currentTemperatureCelsius;
    }

    public void setCurrentTemperatureCelsius(int currentTemperatureCelsius) {
        this.currentTemperatureCelsius = currentTemperatureCelsius;
    }

    public String getCurrentTemperatureUiValue() {
        return currentTemperatureUiValue;
    }

    public void setCurrentTemperatureUiValue(String currentTemperatureUiValue) {
        this.currentTemperatureUiValue = currentTemperatureUiValue;
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
