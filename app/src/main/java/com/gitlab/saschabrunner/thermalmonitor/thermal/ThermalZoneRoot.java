package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.os.RemoteException;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.root.IIPC;

import java.io.File;
import java.util.List;


public class ThermalZoneRoot extends ThermalZoneBase {
    private static final String TAG = "ThermalZoneRoot";

    private IIPC rootIpc;
    private String type;
    private int rootIpcTemperatureFileId;
    private int lastTemperature;

    public ThermalZoneRoot(File sysfsDirectory, IIPC rootIpc) throws RemoteException {
        super(sysfsDirectory);

        this.rootIpc = rootIpc;
        this.type = readType();
        this.rootIpcTemperatureFileId = rootIpc.openFile(getTemperatureFilePath(), 10);
    }

    private String readType() throws RemoteException {
        List<String> type = rootIpc.openAndReadFile(getTypeFilePath());

        if (type.isEmpty()) {
            return null;
        }

        return type.get(0);
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
            this.lastTemperature = Integer.parseInt(rootIpc.readFile(rootIpcTemperatureFileId));
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't read new temperature of thermal zone '" + type + "'", e);
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getLastTemperature() {
        return lastTemperature;
    }
}
