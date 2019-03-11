package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.database.DataSetObserver;

import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FirstTimeSetupPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public FirstTimeSetupPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);

        fragments = new ArrayList<>();
        fragments.add(new FirstTimeSetupWelcomeFragment());
        if (Utils.overlayPermissionRequired()) {
            fragments.add(new FirstTimeSetupOverlayFragment());
        }
        fragments.add(new FirstTimeSetupThermalMonitorFragment());
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    public FirstTimeSetupFragment getItemCasted(int position) {
        return (FirstTimeSetupFragment) fragments.get(position);
    }

    @Override
    public void registerDataSetObserver(@NonNull DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }


}
