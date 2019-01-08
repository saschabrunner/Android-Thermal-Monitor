package com.gitlab.saschabrunner.thermalmonitor;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;


public class Preferences extends PreferenceFragmentCompat { // TODO: PreferenceFragmentCompat

    public Preferences() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_preferences, rootKey);
    }
}
