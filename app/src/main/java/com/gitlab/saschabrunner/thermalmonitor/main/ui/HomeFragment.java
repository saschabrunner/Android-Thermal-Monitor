package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.cpufreq.CPUFreqMonitorValidator;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorService;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitorValidator;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private boolean serviceStarting = false;
    private boolean serviceInErrorState = false;

    private Handler uiUpdateHandler;
    private PeriodicUIUpdater periodicUIUpdater;

    private TextView tvRunningStatus;
    private Button btnToggleService;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = Objects.requireNonNull(getView());
        tvRunningStatus = view.findViewById(R.id.homeRunningStatus);
        btnToggleService = view.findViewById(R.id.homeButtonToggleService);

        uiUpdateHandler = new Handler();
        periodicUIUpdater = new PeriodicUIUpdater(uiUpdateHandler, 1000, 10);
        uiUpdateHandler.post(periodicUIUpdater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiUpdateHandler.removeCallbacks(periodicUIUpdater);
    }

    private boolean startService() {
        // Check compatibility
        if (!validateMonitoringAvailable()) {
            return false;
        }

        // Start service
        Activity activity = Objects.requireNonNull(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(new Intent(getContext(), MonitorService.class));
        } else {
            activity.startService(new Intent(getContext(), MonitorService.class));
        }

        return true;
    }

    private boolean validateMonitoringAvailable() {
        return ThermalMonitorValidator.checkMonitoringAvailable(getContext())
                && CPUFreqMonitorValidator.checkMonitoringAvailable(getContext());
    }

    private void stopService() {
        Activity activity = Objects.requireNonNull(getActivity());
        activity.stopService(new Intent(getContext(), MonitorService.class));
    }

    public void toggleService(View view) {
        if (MonitorService.isServiceRunning()) {
            stopService();
        } else {
            serviceStarting = true;
            updateUiServiceStarting();
            if (!startService()) {
                serviceStarting = false;
                serviceInErrorState = true;
                updateUiServiceError();
            }
        }
    }

    private void updateUiServiceRunning() {
        tvRunningStatus.setText(R.string.running);
        tvRunningStatus.setTextColor(getResources().getColor(R.color.success));
        btnToggleService.setText(R.string.stop);
        btnToggleService.setEnabled(true);
    }

    private void updateUiServiceStarting() {
        tvRunningStatus.setText(R.string.starting);
        tvRunningStatus.setTextColor(getResources().getColor(R.color.warning));
        btnToggleService.setText(R.string.stop);
        btnToggleService.setEnabled(false);
    }

    private void updateUiServiceStopped() {
        tvRunningStatus.setText(R.string.stopped);
        tvRunningStatus.setTextColor(getResources().getColor(R.color.error));
        btnToggleService.setText(R.string.start);
        btnToggleService.setEnabled(true);
    }

    private void updateUiServiceError() {
        tvRunningStatus.setText(R.string.error);
        tvRunningStatus.setTextColor(getResources().getColor(R.color.error));
        btnToggleService.setText(R.string.start);
        btnToggleService.setEnabled(true);
    }

    private class PeriodicUIUpdater implements Runnable {
        private Handler handler;
        private int interval;
        private int waitingTicks;
        private int currentWaitingTicks = 0;

        /**
         * @param handler      Handler to schedule continued execution on.
         * @param interval     Interval between scheduled executions.
         * @param waitingTicks How many executions to wait for the service to start before
         *                     switching to error state.
         */
        private PeriodicUIUpdater(Handler handler, int interval, int waitingTicks) {
            this.handler = handler;
            this.interval = interval;
            this.waitingTicks = waitingTicks;
        }

        /**
         * If service is running: updateUiServiceRunning()
         * If service is starting: Wait maximum waitingTicks while updateUiServiceStarting(),
         * before showing timeout error updateUiServiceError()
         * If service is stopped: updateUiServiceStopped()
         * <p>
         * This code is shoddily written and a rewrite should be considered.
         */
        @Override
        public void run() {
            if (MonitorService.isServiceRunning()) {
                currentWaitingTicks = 0;
                serviceInErrorState = false;
                serviceStarting = false;
                updateUiServiceRunning();
            } else {
                if (serviceStarting) {
                    serviceInErrorState = false;
                    if (currentWaitingTicks < waitingTicks) {
                        currentWaitingTicks++;
                        updateUiServiceStarting();
                    } else {
                        Log.e(TAG, "Timed out waiting for service to start (" +
                                "interval: " + interval + ", " +
                                "waitingTicks: " + waitingTicks + ")");
                        currentWaitingTicks = 0;
                        serviceStarting = false;
                        serviceInErrorState = true;
                        MessageUtils.showInfoDialog(getContext(), R.string.timeout,
                                R.string.timedOutWaitingForServiceToStart);
                        updateUiServiceError();
                    }
                } else if (!serviceInErrorState) {
                    currentWaitingTicks = 0;
                    updateUiServiceStopped();
                }
            }

            handler.postDelayed(this, interval);
        }
    }
}
