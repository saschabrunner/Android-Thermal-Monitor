package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.SharedPreferences;

public interface PreferencesInitializer {
    void init(SettingsFragment fragment, SharedPreferences preferences);
}
