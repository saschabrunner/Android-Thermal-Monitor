package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.os.Bundle;
import android.view.View;

import androidx.preference.PreferenceDialogFragmentCompat;

public class ThermalZonePickerDialogFragment extends PreferenceDialogFragmentCompat {
    public static ThermalZonePickerDialogFragment newInstance(String key) {
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);

        ThermalZonePickerDialogFragment fragment = new ThermalZonePickerDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
