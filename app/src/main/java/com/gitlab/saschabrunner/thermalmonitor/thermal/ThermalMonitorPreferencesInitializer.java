package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.content.SharedPreferences;

import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.ui.PreferencesFragment;
import com.gitlab.saschabrunner.thermalmonitor.main.ui.PreferencesInitializer;
import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.List;

import androidx.preference.MultiSelectListPreference;

public class ThermalMonitorPreferencesInitializer implements PreferencesInitializer {
    @Override
    public void init(PreferencesFragment fragment, SharedPreferences preferences) {
        // Load available thermal zones on click with current settings
        MultiSelectListPreference zones = fragment.findPreference(PreferenceConstants.KEY_THERMAL_MONITOR_THERMAL_ZONES);
        zones.setOnPreferenceClickListener(preference -> {
            ThermalMonitor monitor = new ThermalMonitor();
            List<ThermalZoneInfo> thermalZoneInfos = null;
            try {
                thermalZoneInfos = monitor.getThermalZoneInfos(Utils.getGlobalPreferences(preference.getContext()));
            } catch (MonitorException e) {
                e.printStackTrace();
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
