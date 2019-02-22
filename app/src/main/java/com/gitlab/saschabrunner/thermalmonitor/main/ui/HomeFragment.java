package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorService;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class HomeFragment extends Fragment {
    private boolean serviceStarting = false;

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

        Handler handler = new Handler();
        handler.post(new PeriodicUIUpdater(handler, 1000));
    }

    private void startService() {
        ((MainActivity) getActivity()).checkMonitoringAvailable();
        Activity activity = Objects.requireNonNull(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(new Intent(getContext(), MonitorService.class));
        } else {
            activity.startService(new Intent(getContext(), MonitorService.class));
        }
    }

    private void stopService() {
        Activity activity = Objects.requireNonNull(getActivity());
        activity.stopService(new Intent(getContext(), MonitorService.class));
    }

    public void toggleService(View view) {
        if (MonitorService.isServiceRunning()) {
            stopService();
        } else {
            startService();
            serviceStarting = true;
            updateUiServiceStarting();
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

    private class PeriodicUIUpdater implements Runnable {
        private Handler handler;
        private int interval;

        private PeriodicUIUpdater(Handler handler, int interval) {
            this.handler = handler;
            this.interval = interval;
        }

        @Override
        public void run() {
            if (MonitorService.isServiceRunning()) {
                serviceStarting = false;
                updateUiServiceRunning();
            } else {
                if (serviceStarting) {
                    updateUiServiceStarting();
                } else {
                    updateUiServiceStopped();
                }
            }

            handler.postDelayed(this, interval);
        }
    }
}
