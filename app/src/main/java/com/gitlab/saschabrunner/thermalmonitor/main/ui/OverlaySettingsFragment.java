package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.preference.PreferenceFragmentCompat;


public class OverlaySettingsFragment extends PreferenceFragmentCompat {
    public OverlaySettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings_overlay, rootKey);
    }
}
