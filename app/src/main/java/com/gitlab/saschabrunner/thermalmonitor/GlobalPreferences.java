package com.gitlab.saschabrunner.thermalmonitor;

import android.content.SharedPreferences;
import android.util.Log;

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
