package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.os.RemoteException;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.root.IIPC;

import java.io.File;
import java.util.List;


public class ThermalZoneRoot extends ThermalZoneBase {
    private static final String TAG = "ThermalZoneRoot";

    private IIPC rootIpc;
    private int rootIpcTemperatureFileId;
    private int lastTemperature;
    private int factor;

    public ThermalZoneRoot(File sysfsDirectory, IIPC rootIpc) throws RemoteException {
        super(sysfsDirectory.getAbsolutePath());
        setType(readType());

        this.rootIpc = rootIpc;
        this.rootIpcTemperatureFileId = rootIpc.openFile(getTemperatureFilePath(), 10);
        this.factor = detectFactor(readRawTemperature());
    }

    private String readType() throws RemoteException {
        List<String> type = rootIpc.openAndReadFile(getTypeFilePath());

        if (type.isEmpty()) {
            return null;
        }

        return type.get(0);
    }

    private int readRawTemperature() throws RemoteException {
        return Integer.parseInt(rootIpc.readFile(rootIpcTemperatureFileId));
    }

    @Override
    public void deinit() {
        try {
            rootIpc.closeFile(rootIpcTemperatureFileId);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't close file", e);
        }
    }

    @Override
    public void updateTemperature() {
        try {
            this.lastTemperature = readRawTemperature() / factor;
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't update temperature of thermal zone "
                    + getInfo().getId() + " (" + getInfo().getType() + ")", e);
            this.lastTemperature = -1;
        }
    }

    @Override
    public int getLastTemperature() {
        return lastTemperature;
    }
}
