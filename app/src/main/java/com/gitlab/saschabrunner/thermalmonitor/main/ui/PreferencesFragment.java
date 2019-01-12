package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.preference.PreferenceFragmentCompat;


public class PreferencesFragment extends PreferenceFragmentCompat {
    public PreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_preferences, rootKey);
    }
}
