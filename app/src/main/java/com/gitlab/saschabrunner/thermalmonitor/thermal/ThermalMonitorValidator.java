package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.content.Context;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorPreferences;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootIPCSingleton;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

public class ThermalMonitorValidator {
    private static final String TAG = "ThermalMonitorValidator";

    public static boolean checkMonitoringAvailable(Context context) {
        try {
            return checkMonitoringAvailable(context, ThermalMonitor.Preferences
                    .getPreferencesAllThermalZones(Utils.getGlobalPreferences(context)));
        } catch (MonitorException e) {
            Log.e(TAG, "Illegal configuration for monitor", e);
            MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                    R.string.monitorConfigurationInvalid);
            return false;
        }
    }

    public static boolean checkMonitoringAvailable(Context context,
                                                   MonitorPreferences preferences) {
        ThermalMonitor thermalMonitor;
        if (GlobalPreferences.getInstance().rootEnabled()) {
            Log.v(TAG, "Root enabled, initializing Thermal Monitor with Root IPC");
            try {
                thermalMonitor = new ThermalMonitor(RootIPCSingleton.getInstance(context));
            } catch (RootAccessException e) {
                Log.e(TAG, "Root access has been denied", e);
                MessageUtils.showInfoDialog(context, R.string.rootAccessDenied,
                        R.string.couldNotAcquireRootAccess);
                return false;
            }
        } else {
            Log.v(TAG, "Root disabled, initializing Thermal Monitor without Root IPC");
            thermalMonitor = new ThermalMonitor();
        }

        int thermalMonitoringAvailable = thermalMonitor.checkSupported(preferences);
        boolean success = true;

        switch (thermalMonitoringAvailable) {
            case ThermalMonitor.FAILURE_REASON_OK:
                break;
            case ThermalMonitor.FAILURE_REASON_DIR_NOT_EXISTS:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.sysClassThermalDoesNotExist);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_NO_THERMAL_ZONES_FOUND:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.noThermalZonesFound);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_THERMAL_ZONES_NOT_READABLE_ROOT:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.couldNotReadThermalZones);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TYPE_NO_PERMISSION:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.canNotReadTypeOfAThermalZone);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TEMP_NO_PERMISSION:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.canNotReadTemperatureOfAThermalZone);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_NO_ROOT_IPC:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.noRootIpcObjectPassedToMonitor);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TEMP_NOT_READABLE_ROOT:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.canNotReadTemperatureOfAThermalZoneRoot);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TYPE_NOT_READABLE_ROOT:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.canNotReadTypeOfAThermalZoneRoot);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_ILLEGAL_CONFIGURATION:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.monitorConfigurationInvalid);
                break;
            case ThermalMonitor.FAILURE_REASON_NO_THERMAL_ZONES_ENABLED:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.noValidThermalZonesAreEnabled);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_NO_THERMAL_ZONES_FOUND_ROOT:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.noThermalZonesFoundRoot);
                success = false;
                break;
            default:
                MessageUtils.showInfoDialog(context, R.string.thermalMonitoringNotAvailable,
                        R.string.unknownError);
                success = false;
                break;


        }

        return success;
    }
}
