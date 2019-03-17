package com.gitlab.saschabrunner.thermalmonitor.main.monitor.overlay;

import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;

import java.util.Objects;

public class OverlayConfig {
    private final int gravity;
    private final int backgroundColor;
    private final int textColor;
    private final int textSize;
    private final int labelVisibility;
    private final int labelWidth;

    public OverlayConfig(SharedPreferences preferences, DisplayMetrics displayMetrics) {
        this.gravity = Integer.decode(Objects.requireNonNull(preferences.getString(
                PreferenceConstants.KEY_OVERLAY_GRAVITY,
                PreferenceConstants.DEF_OVERLAY_GRAVITY)));

        this.backgroundColor = preferences.getInt(PreferenceConstants.KEY_OVERLAY_BACKGROUND_COLOR,
                PreferenceConstants.DEF_OVERLAY_BACKGROUND_COLOR);

        this.textColor = preferences.getInt(PreferenceConstants.KEY_OVERLAY_TEXT_COLOR,
                PreferenceConstants.DEF_OVERLAY_TEXT_COLOR);

        this.textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                preferences.getInt(
                        PreferenceConstants.KEY_OVERLAY_TEXT_SIZE,
                        PreferenceConstants.DEF_OVERLAY_TEXT_SIZE),
                displayMetrics);

        this.labelVisibility = preferences.getBoolean(
                PreferenceConstants.KEY_OVERLAY_LABEL_VISIBILITY,
                PreferenceConstants.DEF_OVERLAY_LABEL_VISIBILITY) ? View.VISIBLE : View.GONE;

        this.labelWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                preferences.getInt(
                        PreferenceConstants.KEY_OVERLAY_LABEL_WIDTH,
                        PreferenceConstants.DEF_OVERLAY_LABEL_WIDTH),
                displayMetrics);
    }

    public int getGravity() {
        return gravity;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public int getLabelVisibility() {
        return labelVisibility;
    }

    public int getLabelWidth() {
        return labelWidth;
    }
}
