package com.gitlab.saschabrunner.thermalmonitor;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.root.IIPC;
import com.gitlab.saschabrunner.thermalmonitor.root.RootMain;
import com.topjohnwu.superuser.BusyBox;
import com.topjohnwu.superuser.ContainerApp;
import com.topjohnwu.superuser.Shell;

import java.util.List;

import androidx.annotation.Nullable;
import eu.chainfire.librootjava.RootIPCReceiver;
import eu.chainfire.librootjava.RootJava;

public class App extends ContainerApp {
    private static final String TAG = "App";

    private IIPC rootIpc;
    private IPCReceiver rootIpcReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        GlobalPreferences.init(PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Nullable
    @Override
    public Shell getShell() {
        if (!GlobalPreferences.getInstance().rootEnabled()) {
            Log.w(TAG, "Requested root shell even though root is disabled");
            return null;
        }

        // Initialize root shell the first time it is requested
        if (super.getShell() == null) {
            initRootShell();
        }

        return super.getShell();
    }

    private void initRootShell() {
        // Use internal busybox
        BusyBox.setup(this);

        // Configuration
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        setShell(Shell.newInstance());
    }

    public IIPC getRootIpc() {
        if (!GlobalPreferences.getInstance().rootEnabled()) {
            Log.w(TAG, "Requested root IPC even though root is disabled");
            return null;
        }

        if (rootIpc == null) {
            initRootProcess();
        }

        return rootIpc;
    }

    private void initRootProcess() {
        RootJava.cleanupCache(this);
        List<String> launchScript = RootJava.getLaunchScript(
                this,
                RootMain.class,
                null,
                null,
                null,
                BuildConfig.APPLICATION_ID + ":root");


        rootIpcReceiver = new IPCReceiver(this);
        Shell.su(launchScript.toArray(new String[0])).submit();

        rootIpc = rootIpcReceiver.getIPC(30000);
    }

    private static class IPCReceiver extends RootIPCReceiver<IIPC> {
        private IPCReceiver(Context context) {
            super(context, 0, IIPC.class);
        }

        @Override
        public void onConnect(IIPC ipc) {
            // Nothing to do
            Log.v(TAG, "IPC receiver connected");
        }

        @Override
        public void onDisconnect(IIPC ipc) {
            // Nothing to do
            Log.v(TAG, "IPC receiver disconnected");
        }
    }
}
