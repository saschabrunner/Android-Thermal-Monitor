package com.gitlab.saschabrunner.thermalmonitor;

import com.topjohnwu.superuser.BusyBox;
import com.topjohnwu.superuser.ContainerApp;
import com.topjohnwu.superuser.Shell;

public class App extends ContainerApp {
    private static final String TAG = "App";
    @Override
    public void onCreate() {
        super.onCreate();

        // Use internal busybox
        BusyBox.setup(this);

        // Configuration
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        setShell(Shell.newInstance());
    }
}
