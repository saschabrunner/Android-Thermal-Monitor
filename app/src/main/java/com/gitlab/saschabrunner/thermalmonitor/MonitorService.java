package com.gitlab.saschabrunner.thermalmonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MonitorService extends Service {
    private boolean thermalMonitoringEnabled = true;
    private boolean cpuFreqMonitoringEnabled = true;

    private List<Thread> monitoringThreads = new ArrayList<>();

    private String[] texts = new String[2];
    private NotificationCompat.BigTextStyle notificationBigTextStyle;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
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
        notificationBigTextStyle = new NotificationCompat.BigTextStyle();
        notificationBuilder =
                new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID_DEFAULT)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOnlyAlertOnce(true)
                        .setOngoing(true)
                        .setStyle(notificationBigTextStyle);
        notificationManager = NotificationManagerCompat.from(this);

        // Set the service to a foreground service
        startForeground(Constants.NOTIFICATION_ID_MONITOR, notificationBuilder.build());

        // Initialize monitoring modules
        checkMonitoringAvailable();

        if (thermalMonitoringEnabled) {
            ThermalMonitor thermalMonitor = new ThermalMonitor(this);
            monitoringThreads.add(new Thread(thermalMonitor));
        }

        if (cpuFreqMonitoringEnabled) {
            CPUFreqMonitor cpuFreqMonitor = new CPUFreqMonitor(this);
            monitoringThreads.add(new Thread(cpuFreqMonitor));
        }

        for (Thread monitoringThread : monitoringThreads) {
            monitoringThread.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        for (Thread monitoringThread : monitoringThreads) {
            monitoringThread.interrupt();
            try {
                monitoringThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkMonitoringAvailable() {
        int thermalMonitoringAvailable = ThermalZone.checkMonitoringAvailable();
        if (ThermalZone.FAILURE_REASON_OK != thermalMonitoringAvailable) {
            thermalMonitoringEnabled = false;
        }

        int cpuFreqMonitoringAvailable = CPU.checkMonitoringAvailable();
        if (CPU.FAILURE_REASON_OK != cpuFreqMonitoringAvailable) {
            cpuFreqMonitoringEnabled = false;
        }
    }

    public void setNotificationText(String text, int i) {
        texts[i] = text;
        String newNotificationText = texts[0] + texts[1];

        // Update notification
        notificationBigTextStyle.bigText(newNotificationText);
        notificationManager.notify(
                Constants.NOTIFICATION_ID_MONITOR,
                notificationBuilder.build());
    }
}
