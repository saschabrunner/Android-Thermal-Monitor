package com.gitlab.saschabrunner.thermalmonitor;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utils {
    public static SharedPreferences getGlobalPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static App getApp(Service service) {
        return (App) service.getApplication();
    }

    public static App getApp(Activity activity) {
        return (App) activity.getApplication();
    }
}
