package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SeekBarPreference;

public class CustomSeekBarPreference extends SeekBarPreference {
    private TextView seekbarValue;

    private int stepWidth;

    public CustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray styledAttributes = context.obtainStyledAttributes(
                attrs, R.styleable.CustomSeekBarPreference, defStyleAttr, defStyleRes);
        this.stepWidth = styledAttributes.getInt(
                R.styleable.CustomSeekBarPreference_stepWidth, 1);
        styledAttributes.recycle();
    }

    public CustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.seekBarPreferenceStyle);
    }

    public CustomSeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        seekbarValue = (TextView) view.findViewById(androidx.preference.R.id.seekbar_value);
        SeekBar a = (SeekBar) view.findViewById(androidx.preference.R.id.seekbar);
        a.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress += getMin();
                progress /= stepWidth;
                progress *= stepWidth;
                seekbarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress() + getMin();
                progress /= stepWidth;
                progress *= stepWidth;

                // Actually persist the value
                setValue(progress);
            }
        });
    }
}
