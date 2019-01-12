package com.gitlab.saschabrunner.thermalmonitor.thermal.ui;

import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.preference.PreferenceFragmentCompat;


public class ThermalMonitorPreferencesFragment extends PreferenceFragmentCompat {
    public ThermalMonitorPreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_thermal_monitor_preferences, rootKey);
    }
}
