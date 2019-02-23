package com.gitlab.saschabrunner.thermalmonitor.cpufreq;

import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.preference.PreferenceFragmentCompat;


public class CPUFrequencyMonitorSettingsFragment extends PreferenceFragmentCompat {
    public CPUFrequencyMonitorSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings_cpu_frequency_monitor, rootKey);
    }
}
