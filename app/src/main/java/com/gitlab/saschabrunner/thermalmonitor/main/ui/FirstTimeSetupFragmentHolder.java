package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.util.Log;

public class FirstTimeSetupFragmentHolder {
    private static final String TAG = "FirstTimeSetupFHolder";

    private Class<? extends FirstTimeSetupFragment> fragmentClass;
    private FirstTimeSetupFragment fragment;

    public FirstTimeSetupFragmentHolder(Class<? extends FirstTimeSetupFragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    public FirstTimeSetupFragment getFragment() {
        // Instantiate fragment if it hasn't been set yet
        if (fragment == null) {
            try {
                return fragmentClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                Log.e(TAG, "Couldn't instantiate fragment", e);
            }
        }

        return fragment;
    }

    public void setFragment(FirstTimeSetupFragment fragment) {
        this.fragment = fragment;
    }
}
