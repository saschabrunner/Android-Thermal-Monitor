package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;
import com.topjohnwu.superuser.Shell;

import java.util.Objects;

import androidx.annotation.RequiresApi;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            overlaySettings.setVisible(true);
            overlaySettings.setOnPreferenceClickListener(this::openOverlayPermissionSettings);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean openOverlayPermissionSettings(Preference preference) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + Objects.requireNonNull(getContext()).getPackageName()));
        startActivity(intent);
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
