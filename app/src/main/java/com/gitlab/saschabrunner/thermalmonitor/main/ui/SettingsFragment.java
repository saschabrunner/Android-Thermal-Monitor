package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitorPreferencesInitializer;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import androidx.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        new ThermalMonitorPreferencesInitializer()
                .init(this, Utils.getGlobalPreferences(getContext()));
    }
}
