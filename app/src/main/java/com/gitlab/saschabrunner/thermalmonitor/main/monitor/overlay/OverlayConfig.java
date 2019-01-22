package com.gitlab.saschabrunner.thermalmonitor.main.monitor.overlay;

import android.content.SharedPreferences;

import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;

public class OverlayConfig {
    private int gravity;
    private int backgroundColor;
    private int textColor;
    private int textSize;
    private int labelVisibility;
    private int labelWidth;

    public OverlayConfig(SharedPreferences preferences) {
        this.gravity = preferences.getInt(PreferenceConstants.KEY_OVERLAY_GRAVITY,
                PreferenceConstants.DEF_OVERLAY_GRAVITY);

        this.backgroundColor = preferences.getInt(PreferenceConstants.KEY_OVERLAY_BACKGROUND_COLOR,
                PreferenceConstants.DEF_OVERLAY_BACKGROUND_COLOR);

        this.textColor = preferences.getInt(PreferenceConstants.KEY_OVERLAY_TEXT_COLOR,
                PreferenceConstants.DEF_OVERLAY_TEXT_COLOR);

        this.textSize = preferences.getInt(PreferenceConstants.KEY_OVERLAY_TEXT_SIZE,
                PreferenceConstants.DEF_OVERLAY_TEXT_SIZE);

        this.labelVisibility = preferences.getInt(PreferenceConstants.KEY_OVERLAY_LABEL_VISIBILITY,
                PreferenceConstants.DEF_OVERLAY_LABEL_VISIBILITY);

        this.labelWidth = preferences.getInt(PreferenceConstants.KEY_OVERLAY_LABEL_WIDTH,
                PreferenceConstants.DEF_OVERLAY_LABEL_WIDTH);
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
