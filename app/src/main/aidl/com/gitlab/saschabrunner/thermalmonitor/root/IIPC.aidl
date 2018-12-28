// IIPC.aidl
package com.gitlab.saschabrunner.thermalmonitor.root;

interface IIPC {
    int openFile(String path);

    String readFile(int fileId);

    String openAndReadFile(String path);
}
