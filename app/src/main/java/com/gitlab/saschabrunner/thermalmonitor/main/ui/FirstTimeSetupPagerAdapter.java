package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.view.ViewGroup;

import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FirstTimeSetupPagerAdapter extends FragmentPagerAdapter {
    private List<FirstTimeSetupFragmentHolder> fragments;

    public FirstTimeSetupPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);

        fragments = new ArrayList<>();
        fragments.add(new FirstTimeSetupFragmentHolder(FirstTimeSetupWelcomeFragment.class));
        if (Utils.overlayPermissionRequired()) {
            fragments.add(new FirstTimeSetupFragmentHolder(FirstTimeSetupOverlayFragment.class));
        }
        fragments.add(new FirstTimeSetupFragmentHolder(FirstTimeSetupThermalMonitorFragment.class));
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        FirstTimeSetupFragment fragment =
                (FirstTimeSetupFragment) super.instantiateItem(container, position);

        // Add existing fragment back to list (happens after screen rotation for example)
        fragments.get(position).setFragment(fragment);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return (Fragment) fragments.get(position).getFragment();
    }

    public FirstTimeSetupFragment getItemRaw(int position) {
        return fragments.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
