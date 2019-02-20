package com.gitlab.saschabrunner.thermalmonitor.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utils {
    public static SharedPreferences getGlobalPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
