package com.gitlab.saschabrunner.thermalmonitor.main.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class FirstTimeSetupOverlayFragment extends Fragment implements FirstTimeSetupFragment {
    public FirstTimeSetupOverlayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first_time_setup_overlay, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Objects.requireNonNull(getView()).findViewById(R.id.firstTimeSetupOverlaySettingButton)
                .setOnClickListener(this::openOverlayPermissionSetting);
    }

    @Override
    public boolean onNextScreenRequested() {
        if (Utils.overlayPermissionEnabled(getContext())) {
            return true;
        }

        MessageUtils.showInfoDialog(getContext(), R.string.overlay_permission_not_enabled,
                R.string.overlay_permission_not_enabled_text);

        return false;
    }

    public void openOverlayPermissionSetting(View v) {
        Utils.openOverlayPermissionSetting(getContext());
    }
}
