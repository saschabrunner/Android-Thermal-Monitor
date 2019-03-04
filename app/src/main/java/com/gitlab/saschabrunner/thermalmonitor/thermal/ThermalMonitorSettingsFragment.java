package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;

import java.util.Objects;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class ThermalMonitorSettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "ThermalMonitorSettingsF";

    public ThermalMonitorSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings_thermal_monitor, rootKey);

        // Disable root preference if root access is disabled
        if (!GlobalPreferences.getInstance().rootEnabled()) {
            Preference useRoot = findPreference(PreferenceConstants.KEY_THERMAL_MONITOR_USE_ROOT);
            useRoot.setEnabled(false);
            useRoot.setSummary(R.string.enableRootGloballyFirstToUseThisFeature);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;

        if (preference instanceof ThermalZonePickerPreference) {
            dialogFragment = ThermalZonePickerDialogFragment.newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(Objects.requireNonNull(getFragmentManager()), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
