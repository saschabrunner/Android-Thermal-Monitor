package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitorPreferencesInitializer;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.Objects;

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
        overlaySettings.setOnPreferenceClickListener(preference -> openOverlayPermissionSettings());

        new ThermalMonitorPreferencesInitializer()
                .init(this, Utils.getGlobalPreferences(getContext()));
    }

    private boolean openOverlayPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + Objects.requireNonNull(getContext()).getPackageName()));
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Only available on Android 6.0 and up",
                    Toast.LENGTH_LONG)
                    .show();
        }
        return true;
    }
}
