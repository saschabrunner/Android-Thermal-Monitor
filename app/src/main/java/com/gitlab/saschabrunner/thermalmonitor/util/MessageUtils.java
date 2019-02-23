package com.gitlab.saschabrunner.thermalmonitor.util;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

public class MessageUtils {
    public static void showInfoDialog(Context context, @StringRes int title,
                                      @StringRes int message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel())
                .show();
    }
}
