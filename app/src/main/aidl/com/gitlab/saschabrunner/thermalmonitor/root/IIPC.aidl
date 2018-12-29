// IIPC.aidl
package com.gitlab.saschabrunner.thermalmonitor.root;

interface IIPC {
    List<String> listFiles(String path);

    int openFile(String path, int maxLength);

    String readFile(int fileId);

    String openAndReadFile(String path);
}
