package com.gitlab.saschabrunner.thermalmonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MonitorService extends Service {
    private static final String TAG = "MonitorService";

    private final Lock mutex = new ReentrantLock();
    private final Condition notPaused = mutex.newCondition();
    private boolean monitoringPaused = true;
    private boolean monitoringRunning = true;

    private boolean thermalMonitoringEnabled = true;
    private boolean cpuFreqMonitoringEnabled = true;

    private BroadcastReceiver powerEventReceiver;
    private List<Monitor> monitors = new ArrayList<>();
    private List<Thread> monitoringThreads = new ArrayList<>();

    private String[] texts = new String[2];
    private NotificationCompat.BigTextStyle notificationBigTextStyle;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
        initNotification();

        // Set the service to a foreground service
        startForeground(Constants.NOTIFICATION_ID_MONITOR, notificationBuilder.build());

        initBroadcastReceiver();
        initMonitoring();
    }

    private void initNotification() {
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
    }

    private void initBroadcastReceiver() {
        powerEventReceiver = new PowerEventReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        this.registerReceiver(powerEventReceiver, filter);
    }

    private void initMonitoring() {
        checkMonitoringAvailable();
        continueMonitoring();

        if (thermalMonitoringEnabled) {
            ThermalMonitor thermalMonitor = new ThermalMonitor(this);
            monitoringThreads.add(new Thread(thermalMonitor, "ThermalMonitor"));
        }

        if (cpuFreqMonitoringEnabled) {
            CPUFreqMonitor cpuFreqMonitor = new CPUFreqMonitor(this);
            monitoringThreads.add(new Thread(cpuFreqMonitor, "CPUFreqMonitor"));
        }

        for (Thread monitoringThread : monitoringThreads) {
            monitoringThread.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartComand (startId=" + startId + ")");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        deinitBroadcastReceiver();
        deinitMonitoring();
    }

    private void deinitBroadcastReceiver() {
        unregisterReceiver(powerEventReceiver);
    }

    private void deinitMonitoring() {
        mutex.lock();

        try {
            monitoringRunning = false;
            for(Thread monitoringThread : monitoringThreads) {
                monitoringThread.interrupt();
            }
        } finally {
            mutex.unlock();
        }

        for (Monitor monitor : monitors) {
            monitor.deinit();
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

    private void pauseMonitoring() {
        mutex.lock();
        try {
            monitoringPaused = true;
        } finally {
            mutex.unlock();
        }
    }

    private void continueMonitoring() {
        mutex.lock();
        try {
            monitoringPaused = false;
            notPaused.signalAll();
        } finally {
            mutex.unlock();
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

    /**
     * Called by specific monitor thread to check if it should pause.
     * @return true, when the monitoring service is paused.
     */
    public boolean isMonitoringPaused() {
        mutex.lock();
        try {
            return monitoringPaused;
        } finally {
            mutex.unlock();
        }
    }

    /**
     * If the monitoring service is paused, this method will block until the service is continued.
     */
    public void awaitNotPaused() {
        mutex.lock();
        try {
            if (isMonitoringPaused()) {
                notPaused.await();
            }
        } catch (InterruptedException e) {
           // Nothing to do
        } finally {
            mutex.unlock();
        }
    }

    public boolean isMonitoringRunning() {
        mutex.lock();
        try {
            return monitoringRunning;
        } finally {
            mutex.unlock();
        }
    }

    private class PowerEventReceiver extends BroadcastReceiver {
        private static final String TAG = "PowerEventReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Received " + intent.getAction());

            if (intent.getAction() != null) {
                switch(intent.getAction()) {
                    case Intent.ACTION_SCREEN_OFF:
                        pauseMonitoring();
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        continueMonitoring();
                        break;
                }
            }
        }

        private void pauseMonitoring() {
            Log.v(TAG, "PAUSE MONITORING");
            MonitorService.this.pauseMonitoring();
        }

        private void continueMonitoring() {
            Log.v(TAG, "CONTINUE MONITORING");
            MonitorService.this.continueMonitoring();
        }
    }
}
