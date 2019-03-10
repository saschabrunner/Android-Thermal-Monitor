package com.gitlab.saschabrunner.thermalmonitor.main.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class FirstTimeSetupWelcomeFragment extends Fragment implements FirstTimeSetupFragment {
    public FirstTimeSetupWelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first_time_setup_welcome, container, false);
    }

    @Override
    public boolean onNextScreenRequested() {
        return true;
    }
}
