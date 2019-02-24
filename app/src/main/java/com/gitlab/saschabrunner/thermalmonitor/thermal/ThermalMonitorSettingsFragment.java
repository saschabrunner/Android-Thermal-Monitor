package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.os.Bundle;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootIPCSingleton;
import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.List;

import androidx.preference.MultiSelectListPreference;
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

        // Load available thermal zones on click with current settings
        Preference zones = findPreference(PreferenceConstants.KEY_THERMAL_MONITOR_THERMAL_ZONES);
        zones.setOnPreferenceClickListener(this::populateThermalZoneList);
    }

    /**
     * Load available thermal zones with current settings.
     * TODO: It's the easiest way to load the thermal zones respecting the current user
     * settings, but it's slow and should be replaced
     *
     * @param preference MultiSelectListPreference
     * @return true
     */
    private boolean populateThermalZoneList(Preference preference) {
        ThermalMonitor monitor;
        if (GlobalPreferences.getInstance().rootEnabled()) {
            try {
                monitor = new ThermalMonitor(
                        RootIPCSingleton.getInstance(getContext()));
            } catch (RootAccessException e) {
                Log.e(TAG, "Could not acquire root access", e);
                return false;
            }
        } else {
            monitor = new ThermalMonitor();
        }

        List<ThermalZoneInfo> thermalZoneInfos;
        try {
            thermalZoneInfos = monitor.getThermalZoneInfos(
                    Utils.getGlobalPreferences(preference.getContext()));
        } catch (MonitorException e) {
            Log.e(TAG, "Couldn't get thermal zones", e);
            return false;
        }

        String[] entries = new String[thermalZoneInfos.size()];
        String[] entrieValues = new String[thermalZoneInfos.size()];

        for (int i = 0; i < thermalZoneInfos.size(); i++) {
            ThermalZoneInfo info = thermalZoneInfos.get(i);
            entries[i] = info.getType();
            entrieValues[i] = String.valueOf(info.getId());
        }

        ((MultiSelectListPreference) preference).setEntries(entries);
        ((MultiSelectListPreference) preference).setEntryValues(entrieValues);

        return true;
    }
}
