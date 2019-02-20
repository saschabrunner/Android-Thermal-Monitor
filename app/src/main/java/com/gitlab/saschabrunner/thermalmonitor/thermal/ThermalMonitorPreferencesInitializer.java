package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.content.SharedPreferences;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.ui.PreferencesFragment;
import com.gitlab.saschabrunner.thermalmonitor.main.ui.PreferencesInitializer;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootIPCSingleton;
import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.List;

import androidx.preference.MultiSelectListPreference;

public class ThermalMonitorPreferencesInitializer implements PreferencesInitializer {
    private static final String TAG = "ThermalMonitorPrefInit";

    @Override
    public void init(PreferencesFragment fragment, SharedPreferences preferences) {
        // Load available thermal zones on click with current settings
        /* TODO: It's the easiest way to load the thermal zones respecting the current user
         * settings, but it's slow and should be replaced */
        MultiSelectListPreference zones =
                fragment.findPreference(PreferenceConstants.KEY_THERMAL_MONITOR_THERMAL_ZONES);
        zones.setOnPreferenceClickListener(preference -> {
            ThermalMonitor monitor;
            if (GlobalPreferences.getInstance().rootEnabled()) {
                try {
                    monitor = new ThermalMonitor(
                            RootIPCSingleton.getInstance(fragment.getContext()));
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
        });
    }
}
