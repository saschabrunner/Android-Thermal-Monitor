package com.gitlab.saschabrunner.thermalmonitor.util;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;

import java.util.Collections;
import java.util.Set;

public class PreferenceConstants {
    /* Keys */
    public static final String KEY_ROOT_ENABLED = "rootEnabled";
    public static final String KEY_THERMAL_MONITOR_ENABLED = "thermalMonitorEnabled";
    public static final String KEY_THERMAL_MONITOR_USE_ROOT = "thermalMonitorUseRoot";
    public static final String KEY_THERMAL_MONITOR_REFRESH_INTERVAL = "thermalMonitorRefreshInterval";
    public static final String KEY_THERMAL_MONITOR_THERMAL_ZONES = "thermalMonitorThermalZones";
    public static final String KEY_CPU_FREQ_MONITOR_ENABLED = "cpuFreqMonitorEnabled";
    public static final String KEY_CPU_FREQ_MONITOR_REFRESH_INTERVAL = "cpuFreqMonitorRefreshInterval";
    public static final String KEY_OVERLAY_GRAVITY = "overlayTextGravity";
    public static final String KEY_OVERLAY_BACKGROUND_COLOR = "overlayBackgroundColor";
    public static final String KEY_OVERLAY_TEXT_COLOR = "overlayTextColor";
    public static final String KEY_OVERLAY_TEXT_SIZE = "overlayTextSize";
    public static final String KEY_OVERLAY_LABEL_VISIBILITY = "overlayLabelVisibility";
    public static final String KEY_OVERLAY_LABEL_WIDTH = "overlayLabelWidth";

    /* Defaults */
    public static final boolean DEF_ROOT_ENABLED = false;
    public static final boolean DEF_THERMAL_MONITOR_ENABLED = false;
    public static final boolean DEF_THERMAL_MONITOR_USE_ROOT = false;
    public static final String DEF_THERMAL_MONITOR_REFRESH_INTERVAL = String.valueOf(1000);
    public static final Set<String> DEF_THERMAL_MONITOR_THERMAL_ZONES = Collections.emptySet();
    public static final boolean DEF_CPU_FREQ_MONITOR_ENABLED = false;
    public static final String DEF_CPU_FREQ_MONITOR_REFRESH_INTERVAL = String.valueOf(1000);
    public static final String DEF_OVERLAY_GRAVITY = String.valueOf(Gravity.START);
    public static final String DEF_OVERLAY_BACKGROUND_COLOR = String.valueOf(0x80FFFFFF);
    public static final String DEF_OVERLAY_TEXT_COLOR = String.valueOf(Color.BLACK);
    public static final String DEF_OVERLAY_TEXT_SIZE = String.valueOf(24);
    public static final String DEF_OVERLAY_LABEL_VISIBILITY = String.valueOf(View.VISIBLE);
    public static final String DEF_OVERLAY_LABEL_WIDTH = String.valueOf(260);
}
