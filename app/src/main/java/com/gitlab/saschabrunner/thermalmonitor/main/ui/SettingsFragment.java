package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.gitlab.saschabrunner.thermalmonitor.R;

import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

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
}
