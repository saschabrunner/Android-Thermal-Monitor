package com.gitlab.saschabrunner.thermalmonitor.util;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;

public class PreferenceConstants {
    /* Keys */
    public static final String KEY_ROOT_ENABLED = "rootEnabled";
    public static final String KEY_THERMAL_MONITOR_ENABLED = "thermalMonitorEnabled";
    public static final String KEY_THERMAL_MONITOR_USE_ROOT = "thermalMonitorUseRoot";
    public static final String KEY_THERMAL_MONITOR_REFRESH_INTERVAL = "thermalMonitorRefreshInterval";
    public static final String KEY_CPU_FREQ_MONITOR_ENABLED = "cpuFreqMonitorEnabled";
    public static final String KEY_CPU_FREQ_MONITOR_REFRESH_INTERVAL = "cpuFreqMonitorRefreshInterval";
    public static final String KEY_OVERLAY_GRAVITY = "overlayTextGravity";
    public static final String KEY_OVERLAY_BACKGROUND_COLOR = "overlayTextColor";
    public static final String KEY_OVERLAY_TEXT_COLOR = "overlayTextColor";
    public static final String KEY_OVERLAY_TEXT_SIZE = "overlayTextSize";
    public static final String KEY_OVERLAY_LABEL_VISIBILITY = "overlayLabelVisibility";
    public static final String KEY_OVERLAY_LABEL_WIDTH = "overlayLabelWidth";

    /* Defaults */
    public static final boolean DEF_ROOT_ENABLED = false;
    public static final boolean DEF_THERMAL_MONITOR_ENABLED = false;
    public static final boolean DEF_THERMAL_MONITOR_USE_ROOT = false;
    public static final int DEF_THERMAL_MONITOR_REFRESH_INTERVAL = 1000;
    public static final boolean DEF_CPU_FREQ_MONITOR_ENABLED = false;
    public static final int DEF_CPU_FREQ_MONITOR_REFRESH_INTERVAL = 1000;
    public static final int DEF_OVERLAY_GRAVITY = Gravity.START;
    public static final int DEF_OVERLAY_BACKGROUND_COLOR = 0x80FFFFFF;
    public static final int DEF_OVERLAY_TEXT_COLOR = Color.BLACK;
    public static final int DEF_OVERLAY_TEXT_SIZE = 24;
    public static final int DEF_OVERLAY_LABEL_VISIBILITY = View.VISIBLE;
    public static final int DEF_OVERLAY_LABEL_WIDTH = 260;
}
