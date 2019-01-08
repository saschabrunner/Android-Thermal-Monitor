package com.gitlab.saschabrunner.thermalmonitor;

import android.content.SharedPreferences;

public class GlobalPreferences {
    private static GlobalPreferences instance;

    private SharedPreferences preferences;

    private GlobalPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean rootEnabled() {
        return preferences.getBoolean(PreferenceConstants.KEY_ROOT_ENABLED, false);
    }

    public static void init(SharedPreferences globalPreferences) throws Exception {
        if (instance == null) {
            instance = new GlobalPreferences(globalPreferences);
        } else {
            throw new Exception("TODO");
        }
    }

    public static GlobalPreferences getInstance() {
        return instance;
    }
}
