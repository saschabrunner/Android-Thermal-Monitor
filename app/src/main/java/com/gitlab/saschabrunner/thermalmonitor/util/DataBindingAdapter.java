package com.gitlab.saschabrunner.thermalmonitor.util;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.databinding.BindingAdapter;

public class DataBindingAdapter {
    private static final String TAG = "DataBindingAdapter";

    @BindingAdapter("android:layout_width")
    public static void setLayoutWidth(View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_gravity")
    public static void setLayoutGravity(View view, int gravity) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        if (layoutParams instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) layoutParams).gravity = gravity;
            view.setLayoutParams(layoutParams);
        } else {
            Log.e(TAG, "Unexpected LayoutParams type");
        }
    }
}
