package com.gitlab.saschabrunner.thermalmonitor;

public class OverlayListItem {
    private String label;
    private String value;

    private int id; // Index position for recycler view

    public OverlayListItem() {
        this.id = -1;
    }

    public OverlayListItem(String label, String value) {
        this();
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
