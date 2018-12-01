package com.gitlab.saschabrunner.thermalmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowerEventReceiver extends BroadcastReceiver {
    private static final String TAG = "PowerEventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
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
    }

    private void continueMonitoring() {
        Log.v(TAG, "CONTINUE MONITORING");
    }
}
