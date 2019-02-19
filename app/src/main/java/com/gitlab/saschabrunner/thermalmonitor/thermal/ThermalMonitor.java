package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.Monitor;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorService;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.overlay.OverlayListItem;
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
    @IntDef({FAILURE_REASON_OK, FAILURE_REASON_DIR_NOT_EXISTS, FAILURE_REASON_NO_ENABLED_THERMAL_ZONES,
            FAILURE_REASON_TYPE_NO_PERMISSION, FAILURE_REASON_TEMP_NO_PERMISSION,
            FAILURE_REASON_NO_ROOT_IPC, FAILURE_REASON_TYPE_NOT_READABLE,
            FAILURE_REASON_TEMP_NOT_READABLE, FAILURE_REASON_THERMAL_ZONES_NOT_READABLE})
    public @interface FAILURE_REASON {
    }

    public static final int FAILURE_REASON_OK = 0;
    public static final int FAILURE_REASON_DIR_NOT_EXISTS = 1;
    public static final int FAILURE_REASON_NO_ENABLED_THERMAL_ZONES = 2;
    public static final int FAILURE_REASON_TYPE_NO_PERMISSION = 3;
    public static final int FAILURE_REASON_TEMP_NO_PERMISSION = 4;
    public static final int FAILURE_REASON_NO_ROOT_IPC = 5;
    public static final int FAILURE_REASON_TYPE_NOT_READABLE = 6;
    public static final int FAILURE_REASON_TEMP_NOT_READABLE = 7;
    public static final int FAILURE_REASON_THERMAL_ZONES_NOT_READABLE = 8;

    private final IIPC rootIpc;

    private Preferences preferences;
    private MonitorService monitorService;
    private List<ThermalZoneBase> thermalZones;
    private Map<ThermalZoneBase, OverlayListItem> listItemByThermalZone;

    public ThermalMonitor() {
        this(null);
    }

    public ThermalMonitor(IIPC rootIpc) {
        this.rootIpc = rootIpc;
    }

    @Override
    @FAILURE_REASON
    public int checkSupported(SharedPreferences monitorPreferences) throws MonitorException {
        // Preferences to check support with
        this.preferences = new Preferences(monitorPreferences);

        if (preferences.useRoot) {
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
        List<File> thermalZoneDirs = getThermalZoneDirs();
        if (thermalZoneDirs.isEmpty()) {
            return FAILURE_REASON_NO_ENABLED_THERMAL_ZONES;
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
            thermalZoneDirs = getThermalZoneDirsRoot();
            if (thermalZoneDirs.isEmpty()) {
                return FAILURE_REASON_NO_ENABLED_THERMAL_ZONES;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Got exception while reading thermal zones", e);
            return FAILURE_REASON_THERMAL_ZONES_NOT_READABLE;
        }

        // Check if type is available for every thermal zone
        try {
            for (String thermalZoneDir : thermalZoneDirs) {
                List<String> type = rootIpc.openAndReadFile(
                        ThermalZone.getTypeFilePath(thermalZoneDir));
                if (type == null || type.isEmpty()) {
                    return FAILURE_REASON_TYPE_NOT_READABLE;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Got exception while reading thermal zone type", e);
            return FAILURE_REASON_TYPE_NOT_READABLE;
        }

        // Check if temperature is available for every thermal zone
        try {
            for (String thermalZoneDir : thermalZoneDirs) {
                List<String> temperature = rootIpc.openAndReadFile(
                        ThermalZone.getTemperatureFilePath(thermalZoneDir));

                if (temperature == null || temperature.isEmpty()) {
                    return FAILURE_REASON_TEMP_NOT_READABLE;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Got exception while reading thermal zone temperature", e);
            return FAILURE_REASON_TEMP_NOT_READABLE;
        }

        return FAILURE_REASON_OK;
    }

    @Override
    public void init(MonitorService monitorService, SharedPreferences monitorPreferences)
            throws MonitorException {
        if (checkSupported(monitorPreferences) != FAILURE_REASON_OK) {
            throw new MonitorException(R.string.monitor_not_supported_with_supplied_preferences);
        }

        this.preferences = new Preferences(monitorPreferences);
        this.monitorService = monitorService;
        if (preferences.useRoot) {
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
        this.listItemByThermalZone = new HashMap<>();
        for (ThermalZoneBase thermalZone : thermalZones) {
            OverlayListItem listItem = new OverlayListItem();
            listItem.setLabel(thermalZone.getInfo().getType());
            listItemByThermalZone.put(thermalZone, listItem);
            monitorService.addListItem(listItem);
        }
    }

    public void deinit() {
        for (ThermalZoneBase thermalZone : thermalZones) {
            thermalZone.deinit();
        }
    }

    @Override
    public void run() {
        while (monitorService.isMonitoringRunning()) {
            monitorService.awaitNotPaused();
            Log.v(TAG, "ThermalMonitor update");

            updateThermalZones();

            for (ThermalZoneBase thermalZone : thermalZones) {
                OverlayListItem listItem = listItemByThermalZone.get(thermalZone);
                assert listItem != null;
                listItem.setValue(String.valueOf(thermalZone.getLastTemperature()));
                monitorService.updateListItem(listItem);
            }

            try {
                Thread.sleep(preferences.interval);
            } catch (InterruptedException e) {
                if (monitorService.isMonitoringRunning()) {
                    // No interrupt should happen except when monitor service quits
                    Log.e(TAG, "Unexcpected Interrupt received", e);
                    return;
                }
            }
        }
    }

    public List<ThermalZoneInfo> getThermalZoneInfos(SharedPreferences monitorPreferences)
            throws MonitorException {
        if (checkSupported(monitorPreferences) != FAILURE_REASON_OK) {
            throw new MonitorException(R.string.monitor_not_supported_with_supplied_preferences);
        }

        this.preferences = new Preferences(monitorPreferences);
        if (preferences.useRoot) {
            try {
                this.thermalZones = getThermalZonesRoot(getThermalZoneDirsRoot());
            } catch (RemoteException e) {
                Log.e(TAG, "Failed retrieving thermal zones using root", e);
                throw new MonitorException(
                        R.string.monitor_not_supported_with_supplied_preferences);
            }
        } else {
            this.thermalZones = getThermalZones(getThermalZoneDirs());
        }

        List<ThermalZoneInfo> infos = new ArrayList<>(this.thermalZones.size());
        for (ThermalZoneBase thermalZone : this.thermalZones) {
            infos.add(thermalZone.getInfo());
        }
        return infos;
    }

    private void updateThermalZones() {
        for (ThermalZoneBase thermalZone : thermalZones) {
            thermalZone.updateTemperature();
        }
    }

    private List<File> getThermalZoneDirs() {
        return Arrays.asList(new File(THERMAL_ZONES_DIR).listFiles((dir, name)
                -> name.toLowerCase().matches(THERMAL_ZONE_REGEX)));
    }

    private List<File> getFilteredThermalZoneDirs() {
        List<File> thermalZoneDirs = getThermalZoneDirs();
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

    private static class Preferences {
        private final boolean useRoot;
        private final int interval;
        private final Set<Integer> thermalZoneIds;

        private Preferences(SharedPreferences preferences) throws MonitorException {
            this.useRoot = preferences.getBoolean(
                    PreferenceConstants.KEY_THERMAL_MONITOR_USE_ROOT,
                    PreferenceConstants.DEF_THERMAL_MONITOR_USE_ROOT);

            this.interval = Integer.parseInt(Objects.requireNonNull(preferences.getString(
                    PreferenceConstants.KEY_THERMAL_MONITOR_REFRESH_INTERVAL,
                    PreferenceConstants.DEF_THERMAL_MONITOR_REFRESH_INTERVAL)));

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
    }
}
