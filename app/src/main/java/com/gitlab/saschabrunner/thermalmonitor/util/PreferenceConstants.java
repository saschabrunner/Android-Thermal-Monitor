package com.gitlab.saschabrunner.thermalmonitor.util;

import android.graphics.Color;
import android.view.Gravity;

import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitor;

import java.util.Collections;
import java.util.Set;

public class PreferenceConstants {
    /* Keys */
    public static final String KEY_ROOT_ENABLED = "rootEnabled";
    public static final String KEY_THERMAL_MONITOR_ENABLED = "thermalMonitorEnabled";
    public static final String KEY_THERMAL_MONITOR_USE_ROOT = "thermalMonitorUseRoot";
    public static final String KEY_THERMAL_MONITOR_SCALE = "thermalMonitorScale";
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

    /* Defaults (only provided as fallback, normally the defaults in the preference XMLs should
     * be applied at application launch */
    public static final boolean DEF_ROOT_ENABLED = false;
    public static final boolean DEF_THERMAL_MONITOR_ENABLED = false;
    public static final boolean DEF_THERMAL_MONITOR_USE_ROOT = false;
    public static final String DEF_THERMAL_MONITOR_SCALE = String.valueOf(ThermalMonitor.SCALE_CELSIUS);
    public static final int DEF_THERMAL_MONITOR_REFRESH_INTERVAL = 1000;
    public static final Set<String> DEF_THERMAL_MONITOR_THERMAL_ZONES = Collections.emptySet();
    public static final boolean DEF_CPU_FREQ_MONITOR_ENABLED = true;
    public static final int DEF_CPU_FREQ_MONITOR_REFRESH_INTERVAL = 1000;
    public static final String DEF_OVERLAY_GRAVITY = String.valueOf(Gravity.START);
    public static final int DEF_OVERLAY_BACKGROUND_COLOR = 0x80FFFFFF;
    public static final int DEF_OVERLAY_TEXT_COLOR = Color.BLACK;
    public static final int DEF_OVERLAY_TEXT_SIZE = 10;
    public static final boolean DEF_OVERLAY_LABEL_VISIBILITY = true;
    public static final int DEF_OVERLAY_LABEL_WIDTH = 130;
}
