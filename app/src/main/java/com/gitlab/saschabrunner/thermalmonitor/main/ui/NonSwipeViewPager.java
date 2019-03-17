package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class NonSwipeViewPager extends ViewPager {
    public NonSwipeViewPager(@NonNull Context context) {
        super(context);
    }

    public NonSwipeViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            return performClick();
        }

        // Disallow moving forward with swiping
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Disallow moving forward with swiping
        return false;
    }

    @Override
    public boolean executeKeyEvent(@NonNull KeyEvent event) {
        // Disallow moving forward with arrow keys
        return false;
    }
}
