package com.gitlab.saschabrunner.thermalmonitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkMonitoringAvailable();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MonitorService.class));
        } else {
            startService(new Intent(this, MonitorService.class));
        }
    }

    private void checkMonitoringAvailable() {
        int thermalMonitoringAvailable = ThermalZone.checkMonitoringAvailable();
        switch (thermalMonitoringAvailable) {
            case ThermalZone.FAILURE_REASON_OK:
                break;
            case ThermalZone.FAILURE_REASON_DIR_NOT_EXISTS:
                showInfoDialog("Thermal Monitoring disabled",
                        "/sys/class/thermal does not exist");
                break;
            case ThermalZone.FAILURE_REASON_NO_THERMAL_ZONES:
                showInfoDialog("Thermal Monitoring disabled",
                        "Can't find any thermal zones in /sys/class/thermal");
                break;
            case ThermalZone.FAILURE_REASON_TYPE_NO_PERMISSION:
                showInfoDialog("Thermal Monitoring disabled",
                        "Can't read type of a thermal zone");
                break;
            case ThermalZone.FAILURE_REASON_TEMP_NO_PERMISSION:
                showInfoDialog("Thermal Monitoring disabled",
                        "Can't read temp of a thermal zone");
                break;
        }

        int cpuFreqMonitoringAvailable = CPU.checkMonitoringAvailable();
        switch (cpuFreqMonitoringAvailable) {
            case CPU.FAILURE_REASON_OK:
                break;
            case CPU.FAILURE_REASON_DIR_NOT_EXISTS:
                showInfoDialog("CPU frequency monitoring disabled",
                        "/sys/devices/system/cpu does not exist");
                break;
            case CPU.FAILURE_REASON_DIR_EMPTY:
                showInfoDialog("CPU frequency monitoring disabled",
                        "Can't find any CPUs in /sys/devices/system/cpu");
                break;
            case CPU.FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION:
                showInfoDialog("CPU frequency monitoring disabled",
                        "Can't read frequency of a CPU");
                break;
        }
    }

    private void showInfoDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }
}
