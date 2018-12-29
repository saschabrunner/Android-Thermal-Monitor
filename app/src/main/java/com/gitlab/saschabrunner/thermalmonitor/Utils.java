package com.gitlab.saschabrunner.thermalmonitor;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    public static SharedPreferences getGlobalPreferences(Context context) {
        return context.getSharedPreferences(
                PreferenceConstants.GLOBAL_SETTINGS_NAME, Context.MODE_PRIVATE);
    }
}
