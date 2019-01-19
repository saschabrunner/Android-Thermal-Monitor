package com.gitlab.saschabrunner.thermalmonitor.main;

import android.content.SharedPreferences;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;

public class GlobalPreferences {
    private static final String TAG = "GlobalPreferences";
    private static GlobalPreferences instance;

    private SharedPreferences preferences;

    private GlobalPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean rootEnabled() {
        return preferences.getBoolean(PreferenceConstants.KEY_ROOT_ENABLED,
                PreferenceConstants.DEF_ROOT_ENABLED);
    }

    public boolean thermalMonitorEnabled() {
        return preferences.getBoolean(PreferenceConstants.KEY_THERMAL_MONITOR_ENABLED,
                PreferenceConstants.DEF_THERMAL_MONITOR_ENABLED);
    }

    public boolean cpuFreqMonitorEnabled() {
        return preferences.getBoolean(PreferenceConstants.KEY_CPU_FREQ_MONITOR_ENABLED,
                PreferenceConstants.DEF_CPU_FREQ_MONITOR_ENABLED);
    }

    public static void init(SharedPreferences globalPreferences) {
        if (instance == null) {
            instance = new GlobalPreferences(globalPreferences);
        } else {
            Log.e(TAG, "Global preferences have already been initialized");
        }
    }

    public static GlobalPreferences getInstance() {
        return instance;
    }
}
