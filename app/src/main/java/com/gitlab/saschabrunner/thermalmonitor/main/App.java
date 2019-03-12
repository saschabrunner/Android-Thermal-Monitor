package com.gitlab.saschabrunner.thermalmonitor.main;

import android.app.Application;

import com.gitlab.saschabrunner.thermalmonitor.BuildConfig;
import com.gitlab.saschabrunner.thermalmonitor.R;
import com.topjohnwu.superuser.BusyBoxInstaller;
import com.topjohnwu.superuser.Shell;

import androidx.preference.PreferenceManager;

public class App extends Application {
    private static final String TAG = "App";

    public void onCreate() {
        super.onCreate();
        configureRootShell();
        initPreferences();
    }

    private void configureRootShell() {
        Shell.Config.addInitializers(BusyBoxInstaller.class);
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
    }

    private void initPreferences() {
        GlobalPreferences.init(PreferenceManager.getDefaultSharedPreferences(this));

        // Apply default preferences
        PreferenceManager.setDefaultValues(this,
                R.xml.fragment_settings, true);
        PreferenceManager.setDefaultValues(this,
                R.xml.fragment_settings_overlay, true);
        PreferenceManager.setDefaultValues(this,
                R.xml.fragment_settings_thermal_monitor, true);
        PreferenceManager.setDefaultValues(this,
                R.xml.fragment_settings_cpu_frequency_monitor, true);
    }
}
