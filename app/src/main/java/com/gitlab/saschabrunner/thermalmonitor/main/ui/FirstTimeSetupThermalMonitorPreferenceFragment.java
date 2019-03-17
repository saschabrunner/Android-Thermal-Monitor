package com.gitlab.saschabrunner.thermalmonitor.main.ui;


import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalZonePickerDialogFragment;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalZonePickerPreference;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;
import com.topjohnwu.superuser.Shell;

import java.util.Objects;

import androidx.fragment.app.DialogFragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class FirstTimeSetupThermalMonitorPreferenceFragment extends PreferenceFragmentCompat {
    public FirstTimeSetupThermalMonitorPreferenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings_first_time_setup_thermal_monitor, rootKey);

        CheckBoxPreference useRoot = findPreference("thermalMonitorUseRoot");
        useRoot.setOnPreferenceChangeListener(this::onRootEnabledChanged);
    }

    private boolean onRootEnabledChanged(Preference preference, Object newValue) {
        boolean rootEnabled = (boolean) newValue;

        // Check if root access is allowed
        if (rootEnabled && !Shell.rootAccess()) {
            MessageUtils.showInfoDialog(getContext(), R.string.rootAccessDenied,
                    R.string.couldNotAcquireRootAccess);
            return false;
        }

        // Also globally enable/disable root
        getPreferenceManager().getSharedPreferences().edit().putBoolean(
                PreferenceConstants.KEY_ROOT_ENABLED, rootEnabled).apply();

        return true;
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
