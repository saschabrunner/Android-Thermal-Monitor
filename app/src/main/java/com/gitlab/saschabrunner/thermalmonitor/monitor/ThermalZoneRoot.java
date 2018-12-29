package com.gitlab.saschabrunner.thermalmonitor.monitor;

import android.os.RemoteException;

import com.gitlab.saschabrunner.thermalmonitor.root.IIPC;

import java.io.File;


public class ThermalZoneRoot extends ThermalZoneBase {
    private IIPC rootIpc;
    private String type;
    private int rootIpcFileId;
    private int lastTemperature;

    public ThermalZoneRoot(File sysfsDirectory, IIPC rootIpc) throws RemoteException {
        super(sysfsDirectory);

        this.rootIpc = rootIpc;
        this.type = readType();
        this.rootIpcFileId = rootIpc.openFile(getTemperatureFilePath(), 10);
    }

    private String readType() throws RemoteException {
        return rootIpc.openAndReadFile(getTypeFilePath());
    }

    @Override
    public void deinit() {
        // TODO
    }

    @Override
    public void updateTemperature() {
        try {
            this.lastTemperature = Integer.parseInt(rootIpc.readFile(rootIpcFileId));
        } catch (RemoteException e) {
            // TODO
            e.printStackTrace();
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
