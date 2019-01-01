package com.gitlab.saschabrunner.thermalmonitor;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    public static SharedPreferences getGlobalPreferences(Context context) {
        return context.getSharedPreferences(
                PreferenceConstants.GLOBAL_SETTINGS_NAME, Context.MODE_PRIVATE);
    }

    public static App getApp(Service service) {
        return (App) service.getApplication();
    }

    public static App getApp(Activity activity) {
        return (App) activity.getApplication();
    }
}
