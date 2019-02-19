package com.gitlab.saschabrunner.thermalmonitor.main;

import android.app.Application;
import android.preference.PreferenceManager;

import com.gitlab.saschabrunner.thermalmonitor.BuildConfig;
import com.topjohnwu.superuser.BusyBoxInstaller;
import com.topjohnwu.superuser.Shell;

public class App extends Application {
    private static final String TAG = "App";

    public void onCreate() {
        super.onCreate();
        configureRootShell();
        GlobalPreferences.init(PreferenceManager.getDefaultSharedPreferences(this));
    }

    private void configureRootShell() {
        Shell.Config.addInitializers(BusyBoxInstaller.class);
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
    }
}
