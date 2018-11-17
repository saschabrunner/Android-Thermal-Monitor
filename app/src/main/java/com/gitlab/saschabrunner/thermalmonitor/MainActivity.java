package com.gitlab.saschabrunner.thermalmonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    private boolean thermalMonitoringEnabled = true;
    private boolean cpuFreqMonitoringEnabled = true;

    private List<ThermalZone> thermalZones;
    private List<CPU> cpus;

    private ArrayAdapter<Object> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_DEFAULT,
                            "Default", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("TODO");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Build initial notification
        final NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID_DEFAULT)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOnlyAlertOnce(true)
                        .setOngoing(true)
                        .setStyle(notificationStyle);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        checkMonitoringAvailable();

        List<Object> listContents = new ArrayList<>();

        if (thermalMonitoringEnabled) {
            thermalZones = ThermalZone.getThermalZones();
            listContents.addAll(thermalZones);
        }

        if (cpuFreqMonitoringEnabled) {
            cpus = CPU.getCpus();
            listContents.addAll(cpus);
        }

        listAdapter = new ArrayAdapter<>(this, R.layout.simple_text_view, listContents);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(listAdapter);

        Thread monitoringThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final TextView tvTime = findViewById(R.id.tvTime);

                while (true) {
                    StringBuilder notificationString = new StringBuilder();
                    if (thermalMonitoringEnabled) {
                        updateThermalZones();
                        for (ThermalZone thermalZone : thermalZones) {
                            notificationString.append(thermalZone.toString()).append("\n");
                        }
                    }

                    if (cpuFreqMonitoringEnabled) {
                        updateCpus();
                        for (CPU cpu : cpus) {
                            notificationString.append(cpu.toString()).append("\n");
                        }
                    }


                    // Update notification
                    notificationStyle.bigText(notificationString.toString());
                    notificationManager.notify(
                            Constants.NOTIFICATION_ID_MONITOR,
                            notificationBuilder.build());

                    // Can't touch views from separate thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter.notifyDataSetChanged();
                            tvTime.setText(new Date().toString());
                        }
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            private void updateThermalZones() {
                for (ThermalZone thermalZone : thermalZones) {
                    try {
                        thermalZone.updateTemperature();
                    } catch (IOException e) {
                        // TODO
                        e.printStackTrace();
                    }
                }
            }

            private void updateCpus() {
                for (CPU cpu : cpus) {
                    try {
                        cpu.update();
                    } catch (IOException e) {
                        // TODO
                        e.printStackTrace();
                    }
                }
            }
        });
        monitoringThread.start();
    }

    private void checkMonitoringAvailable() {
        int thermalMonitoringAvailable = ThermalZone.checkMonitoringAvailable();
        if (ThermalZone.FAILURE_REASON_OK != thermalMonitoringAvailable) {
            switch (thermalMonitoringAvailable) {
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
            thermalMonitoringEnabled = false;
        }

        int cpuFreqMonitoringAvailable = CPU.checkMonitoringAvailable();
        if (CPU.FAILURE_REASON_OK != cpuFreqMonitoringAvailable) {
            switch (cpuFreqMonitoringAvailable) {
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
            cpuFreqMonitoringEnabled = false;
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
