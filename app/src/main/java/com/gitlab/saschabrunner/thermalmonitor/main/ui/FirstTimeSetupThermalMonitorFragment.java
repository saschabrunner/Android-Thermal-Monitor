package com.gitlab.saschabrunner.thermalmonitor.main.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitorValidator;
import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;


public class FirstTimeSetupThermalMonitorFragment extends Fragment
        implements FirstTimeSetupFragment {
    private FirstTimeSetupThermalMonitorPreferenceFragment preferenceFragment;

    public FirstTimeSetupThermalMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first_time_setup_thermal_monitor,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceFragment = new FirstTimeSetupThermalMonitorPreferenceFragment();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.firstTimeSetupThermalMonitorSettings, preferenceFragment)
                .commit();
    }

    @Override
    public boolean onNextScreenRequested() {
        CheckBoxPreference preferenceEnabled =
                preferenceFragment.findPreference(PreferenceConstants.KEY_THERMAL_MONITOR_ENABLED);
        if (preferenceEnabled.isChecked()) {
            return ThermalMonitorValidator.checkMonitoringAvailable(getContext());
        }
        return true;
    }
}
