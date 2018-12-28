package com.gitlab.saschabrunner.thermalmonitor.root;

import android.os.IBinder;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.BuildConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import eu.chainfire.librootjava.RootIPC;
import eu.chainfire.librootjava.RootJava;

public class RootMain {
    private static final String TAG = "RootMain";

    public static void main(String[] args) {
        RootJava.restoreOriginalLdLibraryPath();

        IBinder ipc = new IIPC.Stub() {
            @Override
            public int openFile(String path) {
                testRoot();
                return 0;
            }

            @Override
            public String readFile(int fileId) {
                return null;
            }

            @Override
            public String openAndReadFile(String path) {
                return null;
            }
        };

        try {
            new RootIPC(
                    BuildConfig.APPLICATION_ID,
                    ipc,
                    0,
                    -1,
                    true);
        } catch (RootIPC.TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void testRoot() {
        try (BufferedReader br = new BufferedReader(new FileReader("/sys/class/thermal/thermal_zone1/temp"))) {
            Log.v(TAG, br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
