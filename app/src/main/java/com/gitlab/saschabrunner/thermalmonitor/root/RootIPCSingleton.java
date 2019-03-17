package com.gitlab.saschabrunner.thermalmonitor.root;

import android.content.Context;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.BuildConfig;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.topjohnwu.superuser.Shell;

import java.util.List;

import eu.chainfire.librootjava.RootIPCReceiver;
import eu.chainfire.librootjava.RootJava;

public class RootIPCSingleton {
    private static final String TAG = "RootIPCSingleton";
    private static IIPC instance;

    private RootIPCSingleton() {
    }

    private static void init(Context context) throws RootAccessException {
        if (!Shell.rootAccess()) {
            throw new RootAccessDeniedException();
        }

        RootJava.cleanupCache(context);
        List<String> launchScript = RootJava.getLaunchScript(
                context,
                RootMain.class,
                null,
                null,
                null,
                BuildConfig.APPLICATION_ID + ":root");


        IPCReceiver ipcReceiver = new IPCReceiver(context);
        Shell.su(launchScript.toArray(new String[0])).submit();

        instance = ipcReceiver.getIPC(30000);
        if (instance == null) {
            throw new RootAccessIPCTimeoutException();
        }
    }

    public static IIPC getInstance(Context context) throws RootAccessException {
        if (!GlobalPreferences.getInstance().rootEnabled()) {
            Log.e(TAG, "Requested root IPC even though root is disabled");
            throw new RootAccessDisabledException();
        }

        if (instance == null) {
            init(context);
        }

        return instance;
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
