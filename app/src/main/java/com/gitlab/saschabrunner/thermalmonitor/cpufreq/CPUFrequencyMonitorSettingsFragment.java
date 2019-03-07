package com.gitlab.saschabrunner.thermalmonitor.cpufreq;

import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class CPUFrequencyMonitorSettingsFragment extends PreferenceFragmentCompat {
    public CPUFrequencyMonitorSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings_cpu_frequency_monitor, rootKey);

        findPreference("dummyValidatePreferences")
                .setOnPreferenceClickListener(this::checkMonitoringAvailable);
    }

    private boolean checkMonitoringAvailable(Preference preference) {
        if (CPUFreqMonitorValidator.checkMonitoringAvailable(getContext())) {
            MessageUtils.showInfoDialog(getContext(), R.string.success,
                    R.string.theModuleSeemsToBeWorkingCorrectlyOnThisDeviceUsingTheCurrentSettings);
        }
        return true;
    }
}
