package com.gitlab.saschabrunner.thermalmonitor.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.util.Objects;

public class Utils {
    public static SharedPreferences getGlobalPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean overlayPermissionEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }

        return true;
    }

    public static boolean overlayPermissionRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void openOverlayPermissionSetting(Context context) {
        if (overlayPermissionRequired()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + Objects.requireNonNull(context).getPackageName()));
            context.startActivity(intent);
        }
    }
}
