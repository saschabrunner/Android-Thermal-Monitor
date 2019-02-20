package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.SharedPreferences;

public interface PreferencesInitializer {
    void init(PreferencesFragment fragment, SharedPreferences preferences);
}
