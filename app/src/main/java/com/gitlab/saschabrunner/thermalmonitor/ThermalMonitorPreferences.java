package com.gitlab.saschabrunner.thermalmonitor;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;


public class ThermalMonitorPreferences extends PreferenceFragmentCompat {
    public ThermalMonitorPreferences() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_thermal_monitor_preferences, rootKey);
    }
}
