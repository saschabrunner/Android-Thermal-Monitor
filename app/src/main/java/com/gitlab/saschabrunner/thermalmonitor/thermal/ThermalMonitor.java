package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.Monitor;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorController;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorPreferences;
import com.gitlab.saschabrunner.thermalmonitor.root.IIPC;
import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

public class ThermalMonitor implements Runnable, Monitor {
    private static final String TAG = "ThermalMonitor";
    private static final String THERMAL_ZONES_DIR = "/sys/class/thermal";
    private static final String THERMAL_ZONE_REGEX = "(thermal_zone)[0-9]+";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FAILURE_REASON_OK, FAILURE_REASON_DIR_NOT_EXISTS,
            FAILURE_REASON_NO_THERMAL_ZONES_FOUND, FAILURE_REASON_TYPE_NO_PERMISSION,
            FAILURE_REASON_TEMP_NO_PERMISSION, FAILURE_REASON_NO_ROOT_IPC,
            FAILURE_REASON_TYPE_NOT_READABLE_ROOT, FAILURE_REASON_TEMP_NOT_READABLE_ROOT,
            FAILURE_REASON_THERMAL_ZONES_NOT_READABLE_ROOT, FAILURE_REASON_ILLEGAL_CONFIGURATION,
            FAILURE_REASON_NO_THERMAL_ZONES_ENABLED, FAILURE_REASON_NO_THERMAL_ZONES_FOUND_ROOT})
    public @interface FAILURE_REASON {
    }

    /**
     * Everything seems fine with the supplied configuration.
     */
    public static final int FAILURE_REASON_OK = 0;

    /**
     * The directory /sys/class/thermal does not exist or is not readable.
     */
    public static final int FAILURE_REASON_DIR_NOT_EXISTS = 1;

    /**
     * The directory /sys/class/thermal does not contain any readable thermal zones (no root).
     */
    public static final int FAILURE_REASON_NO_THERMAL_ZONES_FOUND = 2;

    /**
     * The file 'type', containing the type name, of a thermal zone is not readable (no root).
     */
    public static final int FAILURE_REASON_TYPE_NO_PERMISSION = 3;

    /**
     * The file 'temp', containing the current temperature, of a thermal zone is not readable
     * (no root).
     */
    public static final int FAILURE_REASON_TEMP_NO_PERMISSION = 4;

    /**
     * No root IPC object was provided to the monitor, even though root is enabled.
     */
    public static final int FAILURE_REASON_NO_ROOT_IPC = 5;

    /**
     * The file 'type', containing the type name, of a thermal zone is not readable (using root).
     */
    public static final int FAILURE_REASON_TYPE_NOT_READABLE_ROOT = 6;

    /**
     * The file 'temp', containing the current temperature, of a thermal zone is not readable
     * (using root).
     */
    public static final int FAILURE_REASON_TEMP_NOT_READABLE_ROOT = 7;

    /**
     * Thermal zones could not be listed (using root).
     */
    public static final int FAILURE_REASON_THERMAL_ZONES_NOT_READABLE_ROOT = 8;

    /**
     * Configuration contains invalid values / values out of range.
     */
    public static final int FAILURE_REASON_ILLEGAL_CONFIGURATION = 9;

    /**
     * No thermal zone has been enabled, but some are available.
     */
    public static final int FAILURE_REASON_NO_THERMAL_ZONES_ENABLED = 10;

    /**
     * The directory /sys/class/thermal does not contain any readable thermal zones (using root).
     */
    public static final int FAILURE_REASON_NO_THERMAL_ZONES_FOUND_ROOT = 11;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SCALE_CELSIUS, SCALE_FAHRENHEIT})
    public @interface TEMPERATURE_SCALE {

    }

    public static final int SCALE_CELSIUS = 0;
    public static final int SCALE_FAHRENHEIT = 1;

    private final IIPC rootIpc;

    private Preferences preferences;
    private MonitorController controller;
    private List<ThermalZoneBase> thermalZones;
    private Map<ThermalZoneBase, ThermalZoneMonitorItem> monitorItemByThermalZone;

    public ThermalMonitor() {
        this(null);
    }

    public ThermalMonitor(IIPC rootIpc) {
        this.rootIpc = rootIpc;
    }

    @Override
    @FAILURE_REASON
    public int checkSupported(MonitorPreferences preferences) {
        // Preferences to check support with
        if (preferences instanceof Preferences) {
            this.preferences = (Preferences) preferences;
        } else {
            return FAILURE_REASON_ILLEGAL_CONFIGURATION;
        }

        if (this.preferences.useRoot) {
            return checkSupportedRoot();
        } else {
            return checkSupportedBasic();
        }
    }

    private int checkSupportedBasic() {
        // Check if dir is present
        File thermalZonesDir = new File(THERMAL_ZONES_DIR);
        if (!thermalZonesDir.exists()) {
            return FAILURE_REASON_DIR_NOT_EXISTS;
        }

        // Check if dirs for thermal zones are present
        List<File> thermalZoneDirs = getFilteredThermalZoneDirs();
        if (thermalZoneDirs.isEmpty()) {
            thermalZoneDirs = getThermalZoneDirs();
            if (thermalZoneDirs.isEmpty()) {
                return FAILURE_REASON_NO_THERMAL_ZONES_FOUND;
            } else {
                return FAILURE_REASON_NO_THERMAL_ZONES_ENABLED;
            }
        }

        // Check if required files are accessible for every thermal zone
        // Right now we assume same permissions for all thermal zones
        for (File zoneDir : thermalZoneDirs) {
            File typeFile =
                    new File(ThermalZoneBase.getTypeFilePath(zoneDir.getAbsolutePath()));
            if (!typeFile.canRead()) {
                return FAILURE_REASON_TYPE_NO_PERMISSION;
            }

            File tempFile =
                    new File(ThermalZoneBase.getTemperatureFilePath(zoneDir.getAbsolutePath()));
            if (!tempFile.canRead()) {
                return FAILURE_REASON_TEMP_NO_PERMISSION;
            }
        }

        return FAILURE_REASON_OK;
    }

    private int checkSupportedRoot() {
        // Check if root IPC is available
        if (this.rootIpc == null) {
            return FAILURE_REASON_NO_ROOT_IPC;
        }

        // Check if dirs for thermal zones are present
        List<String> thermalZoneDirs;
        try {
            thermalZoneDirs = getFilteredThermalZoneDirsRoot();
            if (thermalZoneDirs.isEmpty()) {
                thermalZoneDirs = getThermalZoneDirsRoot();
                if (thermalZoneDirs.isEmpty()) {
                    return FAILURE_REASON_NO_THERMAL_ZONES_FOUND_ROOT;
                } else {
                    return FAILURE_REASON_NO_THERMAL_ZONES_ENABLED;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Got exception while reading thermal zones", e);
            return FAILURE_REASON_THERMAL_ZONES_NOT_READABLE_ROOT;
        }

        // Check if type is available for every thermal zone
        try {
            for (String thermalZoneDir : thermalZoneDirs) {
                List<String> type = rootIpc.openAndReadFile(
                        ThermalZone.getTypeFilePath(thermalZoneDir));
                if (type == null || type.isEmpty()) {
                    return FAILURE_REASON_TYPE_NOT_READABLE_ROOT;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Got exception while reading thermal zone type", e);
            return FAILURE_REASON_TYPE_NOT_READABLE_ROOT;
        }

        // Check if temperature is available for every thermal zone
        try {
            for (String thermalZoneDir : thermalZoneDirs) {
                List<String> temperature = rootIpc.openAndReadFile(
                        ThermalZone.getTemperatureFilePath(thermalZoneDir));

                if (temperature == null || temperature.isEmpty()) {
                    return FAILURE_REASON_TEMP_NOT_READABLE_ROOT;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Got exception while reading thermal zone temperature", e);
            return FAILURE_REASON_TEMP_NOT_READABLE_ROOT;
        }

        return FAILURE_REASON_OK;
    }

    @Override
    public void init(MonitorController controller, MonitorPreferences preferences)
            throws MonitorException {
        if (checkSupported(preferences) != FAILURE_REASON_OK) {
            throw new MonitorException(R.string.monitor_not_supported_with_supplied_preferences);
        }

        this.preferences = (Preferences) preferences;
        this.controller = controller;
        if (this.preferences.useRoot) {
            try {
                this.thermalZones = getThermalZonesRoot(getFilteredThermalZoneDirsRoot());
            } catch (RemoteException e) {
                Log.e(TAG, "Failed retrieving thermal zones using root", e);
                throw new MonitorException(
                        R.string.monitor_not_supported_with_supplied_preferences);
            }
        } else {
            this.thermalZones = getThermalZones(getFilteredThermalZoneDirs());
        }
        this.monitorItemByThermalZone = new HashMap<>();
        for (ThermalZoneBase thermalZone : thermalZones) {
            ThermalZoneMonitorItem item = new ThermalZoneMonitorItem(thermalZone.getInfo());
            item.setName(thermalZone.getInfo().getType());
            monitorItemByThermalZone.put(thermalZone, item);
            controller.addItem(item);
        }
    }

    public void deinit() {
        if (thermalZones != null) {
            for (ThermalZoneBase thermalZone : thermalZones) {
                thermalZone.deinit();
            }
        }
    }

    @Override
    public void run() {
        while (controller.isRunning()) {
            controller.awaitNotPaused();
            Log.v(TAG, "ThermalMonitor update");

            updateThermalZones();

            for (ThermalZoneBase thermalZone : thermalZones) {
                ThermalZoneMonitorItem item = Objects.requireNonNull(
                        monitorItemByThermalZone.get(thermalZone));
                item.setLastTemperature(thermalZone.getLastTemperature());
                item.setValue(temperatureToUiValue(thermalZone.getLastTemperature()));
                controller.updateItem(item);
            }

            try {
                Thread.sleep(preferences.interval);
            } catch (InterruptedException e) {
                if (controller.isRunning()) {
                    // No interrupt should happen except when monitor service quits
                    Log.e(TAG, "Unexpected Interrupt received", e);
                    return;
                }
            }
        }
    }

    /**
     * Creates a string of the temperature, converting the value to the correct scale (celsius
     * or fahrenheit) and adds that scale indicator.
     *
     * @param temperature Temperature in degree celsius.
     * @return String representing the temperature in the set scale with the scale indicator added.
     */
    private String temperatureToUiValue(int temperature) {
        if (preferences.scale == SCALE_CELSIUS) {
            return temperature + "°C";
        } else if (preferences.scale == SCALE_FAHRENHEIT) {
            return toFahrenheit(temperature) + "°F";
        }

        Log.e(TAG, "Unexpected temperature scale '" + preferences.scale + "'");
        return null;
    }

    private int toFahrenheit(int temperatureCelsius) {
        return 32 + 9 * temperatureCelsius / 5;
    }

    private void updateThermalZones() {
        for (ThermalZoneBase thermalZone : thermalZones) {
            thermalZone.updateTemperature();
        }
    }

    private List<File> getThermalZoneDirs() {
        File[] dirs = new File(THERMAL_ZONES_DIR).listFiles((dir, name)
                -> name.toLowerCase().matches(THERMAL_ZONE_REGEX));
        return dirs != null ? Arrays.asList(dirs) : Collections.emptyList();
    }

    private List<File> getFilteredThermalZoneDirs() {
        List<File> thermalZoneDirs = getThermalZoneDirs();

        if (preferences.enableAllThermalZones) {
            return thermalZoneDirs;
        }

        List<File> filteredThermalZoneDirs = new ArrayList<>();
        for (File thermalZoneDir : thermalZoneDirs) {
            // Check if thermal zone is enabled
            if (preferences.thermalZoneIds
                    .contains(ThermalZoneBase.getId(thermalZoneDir.getAbsolutePath()))) {
                filteredThermalZoneDirs.add(thermalZoneDir);
            }
        }

        return filteredThermalZoneDirs;
    }

    private List<String> getThermalZoneDirsRoot() throws RemoteException {
        List<String> dirs = rootIpc.listFiles(THERMAL_ZONES_DIR);

        List<String> thermalZoneDirs = new ArrayList<>();
        for (String dir : dirs) {
            // Check if directory is a thermal zone
            if (dir.toLowerCase()
                    .matches(THERMAL_ZONES_DIR + "/" + THERMAL_ZONE_REGEX)) {
                thermalZoneDirs.add(dir);
            }
        }

        return thermalZoneDirs;
    }

    private @NonNull
    List<String> getFilteredThermalZoneDirsRoot() throws RemoteException {
        List<String> files = getThermalZoneDirsRoot();

        if (preferences.enableAllThermalZones) {
            return files;
        }

        List<String> filteredThermalZoneDirs = new ArrayList<>();
        for (String file : files) {
            // Check if thermal zone is enabled
            if (preferences.thermalZoneIds.contains(ThermalZoneBase.getId(file))) {
                filteredThermalZoneDirs.add(file);
            }
        }
        return filteredThermalZoneDirs;
    }

    private List<ThermalZoneBase> getThermalZones(List<File> thermalZoneDirs) {
        List<ThermalZoneBase> thermalZones = new ArrayList<>(thermalZoneDirs.size());
        for (File dir : thermalZoneDirs) {
            try {
                thermalZones.add(new ThermalZone(dir));
            } catch (IOException e) {
                Log.e(TAG, "Failed initializing thermal zones", e);
                return Collections.emptyList();
            }
        }

        Collections.sort(thermalZones, new ThermalZoneComparator());
        return thermalZones;
    }

    private List<ThermalZoneBase> getThermalZonesRoot(List<String> thermalZoneDirs) {
        List<ThermalZoneBase> thermalZones = new ArrayList<>(thermalZoneDirs.size());
        try {
            for (String dir : thermalZoneDirs) {
                thermalZones.add(new ThermalZoneRoot(new File(dir), rootIpc));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed initializing thermal zones", e);
            return Collections.emptyList();
        }

        Collections.sort(thermalZones, new ThermalZoneComparator());
        return thermalZones;
    }

    private static class ThermalZoneComparator implements Comparator<ThermalZoneBase> {
        @Override
        public int compare(ThermalZoneBase o1, ThermalZoneBase o2) {
            if (o1.getInfo().getId() > o2.getInfo().getId()) {
                return 1;
            } else if (o1.getInfo().getId() < o2.getInfo().getId()) {
                return -1;
            }

            return 0;
        }
    }

    public static class Preferences extends MonitorPreferences {
        private final boolean useRoot;
        private final int scale;
        private final int interval;
        private final boolean enableAllThermalZones; // If true, overrides thermalZoneIds setting
        private final Set<Integer> thermalZoneIds;

        private Preferences(SharedPreferences preferences, boolean enableAllThermalZones)
                throws MonitorException {
            this.useRoot = preferences.getBoolean(
                    PreferenceConstants.KEY_THERMAL_MONITOR_USE_ROOT,
                    PreferenceConstants.DEF_THERMAL_MONITOR_USE_ROOT);

            this.scale = Integer.parseInt(Objects.requireNonNull(preferences.getString(
                    PreferenceConstants.KEY_THERMAL_MONITOR_SCALE,
                    PreferenceConstants.DEF_THERMAL_MONITOR_SCALE)));

            this.interval = preferences.getInt(
                    PreferenceConstants.KEY_THERMAL_MONITOR_REFRESH_INTERVAL,
                    PreferenceConstants.DEF_THERMAL_MONITOR_REFRESH_INTERVAL);

            this.enableAllThermalZones = enableAllThermalZones;
            Set<String> thermalZonesSet = Objects.requireNonNull(preferences.getStringSet(
                    PreferenceConstants.KEY_THERMAL_MONITOR_THERMAL_ZONES,
                    PreferenceConstants.DEF_THERMAL_MONITOR_THERMAL_ZONES));
            this.thermalZoneIds = new HashSet<>();
            for (String thermalZoneId : thermalZonesSet) {
                this.thermalZoneIds.add(Integer.parseInt(thermalZoneId));
            }

            validate();
        }

        private void validate() throws MonitorException {
            // Let's not allow faster refresh intervals
            if (interval < 500) {
                throw new MonitorException(R.string.interval_must_be_at_least_500ms);
            }
        }

        public static Preferences getPreferences(SharedPreferences preferences)
                throws MonitorException {
            return new Preferences(preferences, false);
        }

        public static Preferences getPreferencesAllThermalZones(SharedPreferences preferences)
                throws MonitorException {
            return new Preferences(preferences, true);
        }
    }
}
