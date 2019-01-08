package com.gitlab.saschabrunner.thermalmonitor.monitor;

import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.MonitorService;
import com.gitlab.saschabrunner.thermalmonitor.OverlayListItem;
import com.gitlab.saschabrunner.thermalmonitor.PreferenceConstants;
import com.gitlab.saschabrunner.thermalmonitor.root.IIPC;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.IntDef;

public class ThermalMonitor implements Runnable, Monitor {
    private static final String TAG = "ThermalMonitor";
    private static final String THERMAL_ZONES_DIR = "/sys/class/thermal";
    private static final String THERMAL_ZONE_REGEX = "(thermal_zone)[0-9]+";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FAILURE_REASON_OK, FAILURE_REASON_DIR_NOT_EXISTS, FAILURE_REASON_NO_THERMAL_ZONES,
            FAILURE_REASON_TYPE_NO_PERMISSION, FAILURE_REASON_TEMP_NO_PERMISSION,
            FAILURE_REASON_NO_ROOT_IPC})
    public @interface FAILURE_REASON {
    }

    public static final int FAILURE_REASON_OK = 0;
    public static final int FAILURE_REASON_DIR_NOT_EXISTS = 1;
    public static final int FAILURE_REASON_NO_THERMAL_ZONES = 2;
    public static final int FAILURE_REASON_TYPE_NO_PERMISSION = 3;
    public static final int FAILURE_REASON_TEMP_NO_PERMISSION = 4;
    public static final int FAILURE_REASON_NO_ROOT_IPC = 5;

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
    public int checkSupported(SharedPreferences monitorPreferences) {
        // Preferences to check support with
        Preferences preferences = new Preferences(monitorPreferences);

        if (preferences.useRoot) {
            if (this.rootIpc == null) {
                return FAILURE_REASON_NO_ROOT_IPC;
            }

            try {
                List<String> thermalZoneDirs = filterThermalZonesRoot();
                if (thermalZoneDirs.isEmpty()) {
                    return FAILURE_REASON_NO_THERMAL_ZONES;
                }

                // TODO: Check if we can read the needed files
            } catch (RemoteException e) {
                // TODO
            }

            return FAILURE_REASON_OK;
        } else {
            // Check if dir is present
            File thermalZonesDir = new File(THERMAL_ZONES_DIR);
            if (!thermalZonesDir.exists()) {
                return FAILURE_REASON_DIR_NOT_EXISTS;
            }

            // Check if dirs for thermal zones are present
            File[] thermalZoneDirs = filterThermalZones(thermalZonesDir);
            if (thermalZoneDirs == null || thermalZoneDirs.length == 0) {
                return FAILURE_REASON_NO_THERMAL_ZONES;
            }

            // Check if required files are accessible for every thermal zone
            // Right now we assume same permissions for all thermal zones
            for (File zoneDir : thermalZoneDirs) {
                File typeFile =
                        new File(ThermalZone.getTypeFilePath(zoneDir.getAbsolutePath()));
                if (!typeFile.canRead()) {
                    return FAILURE_REASON_TYPE_NO_PERMISSION;
                }

                File tempFile =
                        new File(ThermalZone.getTemperatureFilePath(zoneDir.getAbsolutePath()));
                if (!tempFile.canRead()) {
                    return FAILURE_REASON_TEMP_NO_PERMISSION;
                }
            }

            return FAILURE_REASON_OK;
        }
    }

    @Override
    public void init(MonitorService monitorService, SharedPreferences monitorPreferences) {
        if (checkSupported(monitorPreferences) != FAILURE_REASON_OK) {
            throw new RuntimeException("TODO");
        }

        this.preferences = new Preferences(monitorPreferences);
        this.monitorService = monitorService;

        if (preferences.useRoot) {
            this.thermalZones = getThermalZonesRoot();
        } else {
            this.thermalZones = getThermalZones();
        }
        this.listItemByThermalZone = new HashMap<>();
        for (ThermalZoneBase thermalZone : thermalZones) {
            OverlayListItem listItem = new OverlayListItem();
            listItem.setLabel(thermalZone.getType());
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (monitorService.isMonitoringRunning()) {
                    // No interrupt should happen except when monitor service quits
                    Log.e(TAG, "Unexcpected Interrupt received", e);
                    return;
                }
            }
        }
    }

    private void updateThermalZones() {
        for (ThermalZoneBase thermalZone : thermalZones) {
            thermalZone.updateTemperature();
        }
    }

    private File[] filterThermalZones(File thermalZonesDir) {
        return thermalZonesDir.listFiles((dir, name) -> {
            String lcName = name.toLowerCase();
            return lcName.matches(THERMAL_ZONE_REGEX);
        });
    }

    private List<String> filterThermalZonesRoot() throws RemoteException {
        List<String> files = rootIpc.listFiles(THERMAL_ZONES_DIR);
        List<String> filteredThermalZoneDirs = new ArrayList<>();
        for (String file : files) {
            if (file.toLowerCase().matches(THERMAL_ZONES_DIR + "/" + THERMAL_ZONE_REGEX)) {
                filteredThermalZoneDirs.add(file);
            }
        }
        return filteredThermalZoneDirs;
    }

    private List<ThermalZoneBase> getThermalZones() {
        File thermalZonesDir = new File(THERMAL_ZONES_DIR);
        File[] thermalZoneDirs = filterThermalZones(thermalZonesDir);

        List<ThermalZoneBase> thermalZones = new ArrayList<>(thermalZoneDirs.length);
        for (File dir : thermalZoneDirs) {
            try {
                thermalZones.add(new ThermalZone(dir));
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        return thermalZones;
    }

    private List<ThermalZoneBase> getThermalZonesRoot() {
        try {
            List<String> thermalZoneDirs = filterThermalZonesRoot();

            List<ThermalZoneBase> thermalZones = new ArrayList<>(thermalZoneDirs.size());
            for (String dir : thermalZoneDirs) {
                thermalZones.add(new ThermalZoneRoot(new File(dir), rootIpc));
            }

            return thermalZones;
        } catch (RemoteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

    }

    private static class Preferences {
        private final boolean useRoot;

        private Preferences(SharedPreferences preferences) {
            this.useRoot = preferences.getBoolean(
                    PreferenceConstants.KEY_THERMAL_MONITOR_USE_ROOT, false);
        }
    }
}
