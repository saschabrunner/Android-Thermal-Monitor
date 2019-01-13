package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;


public class PreferencesFragment extends PreferenceFragmentCompat {
    public PreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_preferences, rootKey);

        setupThermalMonitorPreferences();
        setupCpuFreqMonitorPreferences();
    }

    private void setupThermalMonitorPreferences() {
        EditTextPreference refreshInterval = findPreference("thermalMonitorRefreshInterval");
        refreshInterval.setOnBindEditTextListener(
                editText -> editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER));
    }

    private void setupCpuFreqMonitorPreferences() {
        EditTextPreference refreshInterval = findPreference("cpuFreqMonitorRefreshInterval");
        refreshInterval.setOnBindEditTextListener(
                editText -> editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER));
    }
}
