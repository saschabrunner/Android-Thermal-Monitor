package com.gitlab.saschabrunner.thermalmonitor.main.monitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.cpufreq.CPUFreqMonitor;
import com.gitlab.saschabrunner.thermalmonitor.databinding.OverlayBinding;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.overlay.OverlayConfig;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.overlay.OverlayListAdapter;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.overlay.OverlayListItem;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootIPCSingleton;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitor;
import com.gitlab.saschabrunner.thermalmonitor.util.Constants;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MonitorService extends Service {
    private static final String TAG = "MonitorService";

    private final Lock mutex = new ReentrantLock();
    private final Condition notPaused = mutex.newCondition();
    private boolean monitoringPaused = true;
    private boolean monitoringRunning = true;

    private PowerEventReceiver powerEventReceiver;
    private List<Monitor> monitors = new ArrayList<>();
    private List<Thread> monitoringThreads = new ArrayList<>();

    private View overlayView;
    private NotificationCompat.Builder notificationBuilder;
    private OverlayListAdapter listAdapter;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");

        initNotification();

        // Set the service to a foreground service
        startForeground(Constants.NOTIFICATION_ID_MONITOR, notificationBuilder.build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this,
                        "Overlay permission not enabled, open overlay settings to enable",
                        Toast.LENGTH_LONG).show();
                stopSelf();
                return;
            }
        }

        initBroadcastReceiver();
        initOverlay();
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
        notificationBuilder =
                new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID_DEFAULT)
                        .setSmallIcon(R.drawable.ic_stat_default)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOnlyAlertOnce(true)
                        .setOngoing(true)
                        .setContentTitle("Thermal Monitor Service is running");
    }

    private void initBroadcastReceiver() {
        powerEventReceiver = new PowerEventReceiver();
        powerEventReceiver.register(this);
    }

    private void initOverlay() {
        OverlayConfig overlayConfig = new OverlayConfig(Utils.getGlobalPreferences(this));

        // Inflate layout
        OverlayBinding overlayViewBinding = OverlayBinding.inflate(LayoutInflater.from(this));
        overlayViewBinding.setConfig(overlayConfig);
        this.overlayView = overlayViewBinding.getRoot();


        // Create layout params
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);

        // Add layout view
        windowManager.addView(overlayView, layoutParams);

        // Initialize recycler view
        RecyclerView recyclerView = overlayView.findViewById(R.id.overlayRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Disable animations (otherwise excessive ViewHolders are used in notifyItemChanged())
        recyclerView.setItemAnimator(null);

        // Set adapter on list view
        listAdapter = new OverlayListAdapter(overlayConfig);
        recyclerView.setAdapter(listAdapter);
    }

    private void initMonitoring() {
        continueMonitoring();

        try {
            if (GlobalPreferences.getInstance().thermalMonitorEnabled()) {
                initThermalMonitoring();
            }

            if (GlobalPreferences.getInstance().cpuFreqMonitorEnabled()) {
                initCpuFreqMonitoring();
            }
        } catch (MonitorException e) {
            Log.e(TAG, e.getMessage(this), e);
            stopWithMessage("Monitor exited with exception (check compatibility)");
        }

        for (Thread monitoringThread : monitoringThreads) {
            monitoringThread.start();
        }
    }

    private void initThermalMonitoring() throws MonitorException {
        ThermalMonitor thermalMonitor;
        if (GlobalPreferences.getInstance().rootEnabled()) {
            Log.v(TAG, "Root enabled, initializing Thermal Monitor with Root IPC");
            try {
                thermalMonitor = new ThermalMonitor(RootIPCSingleton.getInstance(this));
            } catch (RootAccessException e) {
                Log.e(TAG, "Service could not acquire root access", e);
                stopWithMessage("Service could not acquire root access");
                return;
            }

        } else {
            Log.v(TAG, "Root disabled, initializing Thermal Monitor without Root IPC");
            thermalMonitor = new ThermalMonitor();
        }

        if (thermalMonitor.checkSupported(Utils.getGlobalPreferences(this))
                == ThermalMonitor.FAILURE_REASON_OK) {
            thermalMonitor.init(this, Utils.getGlobalPreferences(this));
            monitors.add(thermalMonitor);
            monitoringThreads.add(new Thread(thermalMonitor, "ThermalMonitor"));
        } else {
            stopWithMessage("Thermal monitor not supported with current configuration");
        }
    }

    private void initCpuFreqMonitoring() throws MonitorException {
        CPUFreqMonitor cpuFreqMonitor = new CPUFreqMonitor();
        if (cpuFreqMonitor.checkSupported(Utils.getGlobalPreferences(this))
                == CPUFreqMonitor.FAILURE_REASON_OK) {
            cpuFreqMonitor.init(this, Utils.getGlobalPreferences(this));
            monitors.add(cpuFreqMonitor);
            monitoringThreads.add(new Thread(cpuFreqMonitor, "CPUFreqMonitor"));
        } else {
            stopWithMessage("CPU frequency monitor not supported with current configuration");
        }
    }

    private void stopWithMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartComand (startId=" + startId + ")");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        deinitMonitoring();
        deinitOverlay();
        deinitBroadcastReceiver();
    }

    private void deinitMonitoring() {
        mutex.lock();

        try {
            monitoringRunning = false;
            for (Thread monitoringThread : monitoringThreads) {
                monitoringThread.interrupt();
            }
        } finally {
            mutex.unlock();
        }

        for (Monitor monitor : monitors) {
            monitor.deinit();
        }
    }

    private void deinitOverlay() {
        if (overlayView != null) {
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.removeViewImmediate(overlayView);
        }
    }

    private void deinitBroadcastReceiver() {
        if (powerEventReceiver != null) {
            powerEventReceiver.unregister(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    public void addListItem(OverlayListItem listItem) {
        listAdapter.addListItem(listItem);
    }

    public void updateListItem(OverlayListItem listItem) {
        listAdapter.updateListItem(listItem);
    }

    /**
     * Called by specific monitor thread to check if it should pause.
     *
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

        private boolean registered = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Received " + intent.getAction());

            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Intent.ACTION_SCREEN_OFF:
                        pauseMonitoring();
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        continueMonitoring();
                        break;
                }
            }
        }

        /**
         * Used to avoid multiple registrations.
         *
         * @param context Context to register receiver on.
         * @return result from {@link Context#registerReceiver(BroadcastReceiver, IntentFilter)}
         * or null if already registered.
         */
        public Intent register(Context context) {
            if (!registered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                filter.addAction(Intent.ACTION_SCREEN_ON);
                Intent res = context.registerReceiver(this, filter);
                registered = true;
                return res;
            }

            return null;
        }

        /**
         * Used to avoid unregistering when already unregistered.
         *
         * @param context Context to unregister receiver from.
         */
        public void unregister(Context context) {
            if (registered) {
                context.unregisterReceiver(this);
            }
        }

        private void pauseMonitoring() {
            Log.v(TAG, "Pause monitoring");
            MonitorService.this.pauseMonitoring();
        }

        private void continueMonitoring() {
            Log.v(TAG, "Continue monitoring");
            MonitorService.this.continueMonitoring();
        }
    }
}
