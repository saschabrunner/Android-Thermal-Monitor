package com.gitlab.saschabrunner.thermalmonitor.util;

public class PreferenceConstants {
    public static final String GLOBAL_SETTINGS_NAME = "com.gitlab.saschabrunner.thermalmonitor.preferences";

    /* Keys */
    public static final String KEY_ROOT_ENABLED = "rootEnabled";
    public static final String KEY_THERMAL_MONITOR_USE_ROOT = "thermalMonitorUseRoot";
    public static final String KEY_THERMAL_MONITOR_REFRESH_INTERVAL = "thermalMonitorRefreshInterval";
    public static final String KEY_CPU_FREQ_MONITOR_REFRESH_INTERVAL = "cpuFreqMonitorRefreshInterval";

    /* Defaults */
    public static final boolean DEF_ROOT_ENABLED = false;
    public static final boolean DEF_THERMAL_MONITOR_USE_ROOT = false;
    public static final int DEF_THERMAL_MONITOR_REFRESH_INTERVAL = 1000;
    public static final int DEF_CPU_FREQ_MONITOR_REFRESH_INTERVAL = 1000;
}
