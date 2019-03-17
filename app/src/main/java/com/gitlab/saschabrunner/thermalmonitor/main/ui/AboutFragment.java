package com.gitlab.saschabrunner.thermalmonitor.main.ui;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {
    private static final String TAG = "AboutFragment";

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Context context = getContext();

        if (context != null) {
            try {
                ((TextView) view.findViewById(R.id.aboutVersionValue)).setText(
                        context.getPackageManager()
                                .getPackageInfo(getContext().getPackageName(), 0)
                                .versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Unexpected exception from package manager", e);
            }
        }
    }

    public void showLicenses(View view) {
        startActivity(new Intent(getContext(), LicensesActivity.class));
    }
}
