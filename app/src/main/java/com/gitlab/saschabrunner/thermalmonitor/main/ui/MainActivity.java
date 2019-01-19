package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.cpufreq.CPUFreqMonitor;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorService;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitor;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void checkMonitoringAvailable() {
        boolean success = true;

        ThermalMonitor thermalMonitor;
        if (GlobalPreferences.getInstance().rootEnabled()) {
            Log.v(TAG, "Root enabled, initializing Thermal Monitor with Root IPC");
            try {
                thermalMonitor = new ThermalMonitor(Utils.getApp(this).getRootIpc());
            } catch (RootAccessException e) {
                Log.e(TAG, "Root access has been denied", e);
                showInfoDialog("Root access denied", "Could not acquire root access");
                return;
            }
        } else {
            Log.v(TAG, "Root disabled, initializing Thermal Monitor without Root IPC");
            thermalMonitor = new ThermalMonitor();
        }

        try {
            int thermalMonitoringAvailable =
                    thermalMonitor.checkSupported(Utils.getGlobalPreferences(this));
            switch (thermalMonitoringAvailable) {
                case ThermalMonitor.FAILURE_REASON_OK:
                    break;
                case ThermalMonitor.FAILURE_REASON_DIR_NOT_EXISTS:
                    showInfoDialog("Thermal Monitoring disabled",
                            "/sys/class/thermal does not exist");
                    success = false;
                    break;
                case ThermalMonitor.FAILURE_REASON_NO_THERMAL_ZONES:
                    showInfoDialog("Thermal Monitoring disabled",
                            "Can't find any thermal zones in /sys/class/thermal");
                    success = false;
                    break;
                case ThermalMonitor.FAILURE_REASON_TYPE_NO_PERMISSION:
                    showInfoDialog("Thermal Monitoring disabled",
                            "Can't read type of a thermal zone");
                    success = false;
                    break;
                case ThermalMonitor.FAILURE_REASON_TEMP_NO_PERMISSION:
                    showInfoDialog("Thermal Monitoring disabled",
                            "Can't read temp of a thermal zone");
                    success = false;
                    break;
                case ThermalMonitor.FAILURE_REASON_NO_ROOT_IPC:
                    showInfoDialog("Thermal Monitoring disabled",
                            "No root IPC object passed to monitor " +
                                    "(root globally disabled?)");
                    success = false;
                    break;
                case ThermalMonitor.FAILURE_REASON_TEMP_NOT_READABLE:
                    showInfoDialog("Thermal Monitoring disabled",
                            "Can't read temp of a thermal zone");
                    success = false;
                    break;
                case ThermalMonitor.FAILURE_REASON_TYPE_NOT_READABLE:
                    showInfoDialog("Thermal Monitoring disabled",
                            "Can't read type of a thermal zone");
                    success = false;
                    break;
                default:
                    showInfoDialog("Thermal Monitoring disabled",
                            "Unknown error");
                    success = false;
                    break;
            }

            CPUFreqMonitor cpuFreqMonitor = new CPUFreqMonitor();
            int cpuFreqMonitoringAvailable =
                    cpuFreqMonitor.checkSupported(Utils.getGlobalPreferences(this));
            switch (cpuFreqMonitoringAvailable) {
                case CPUFreqMonitor.FAILURE_REASON_OK:
                    break;
                case CPUFreqMonitor.FAILURE_REASON_DIR_NOT_EXISTS:
                    showInfoDialog("CPU frequency monitoring disabled",
                            "/sys/devices/system/cpu does not exist");
                    success = false;
                    break;
                case CPUFreqMonitor.FAILURE_REASON_DIR_EMPTY:
                    showInfoDialog("CPU frequency monitoring disabled",
                            "Can't find any CPUs in /sys/devices/system/cpu");
                    success = false;
                    break;
                case CPUFreqMonitor.FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION:
                    showInfoDialog("CPU frequency monitoring disabled",
                            "Can't read frequency of a CPU");
                    success = false;
                    break;
            }
        } catch (MonitorException e) {
            showInfoDialog("Monitor configuration invalid", e.getMessage(this));
            success = false;
        }

        if (success) {
            showInfoDialog("Checks succeeded",
                    "All modules should be working with the current configuration.");
        }
    }

    private void showInfoDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openOverlayPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MonitorService.class));
        } else {
            startService(new Intent(this, MonitorService.class));
        }
    }

    private void stopService() {
        stopService(new Intent(this, MonitorService.class));
    }

    public void openOverlayPermissionSettings(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openOverlayPermissionSettings();
        } else {
            Toast.makeText(this, "Only available on Android 6.0 and up",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void startService(View view) {
        startService();
    }

    public void stopService(View view) {
        stopService();
    }

    public void showLicenses(View view) {
        startActivity(new Intent(this, LicensesActivity.class));
    }

    public void showPreferences(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.temporaryPreferencesView, new PreferencesFragment())
                .commit();
    }

    public void checkCompatibility(View view) {
        checkMonitoringAvailable();
    }
}
