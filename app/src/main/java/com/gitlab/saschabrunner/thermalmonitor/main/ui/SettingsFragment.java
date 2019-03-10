package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.os.Bundle;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;
import com.topjohnwu.superuser.Shell;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        CheckBoxPreference rootEnabled = findPreference("rootEnabled");
        rootEnabled.setOnPreferenceChangeListener(this::onRootEnabledChanged);

        Preference overlaySettings = findPreference("dummyOverlaySettings");
        if (Utils.overlayPermissionRequired()) {
            overlaySettings.setVisible(true);
            overlaySettings.setOnPreferenceClickListener(this::openOverlayPermissionSettings);
        }
    }

    private boolean openOverlayPermissionSettings(Preference preference) {
        Utils.openOverlayPermissionSetting(getContext());
        return true;
    }

    private boolean onRootEnabledChanged(Preference preference, Object newValue) {
        boolean rootEnabled = (boolean) newValue;

        // Check if root access is allowed
        if (rootEnabled && !Shell.rootAccess()) {
            MessageUtils.showInfoDialog(getContext(), R.string.rootAccessDenied,
                    R.string.couldNotAcquireRootAccess);
            return false;
        }

        return true;
    }
}
