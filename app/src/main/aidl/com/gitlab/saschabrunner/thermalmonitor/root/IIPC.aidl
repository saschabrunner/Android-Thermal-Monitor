// IIPC.aidl
package com.gitlab.saschabrunner.thermalmonitor.root;

interface IIPC {
    List<String> listFiles(String path);

    int openFile(String path, int maxLength);

    boolean closeFile(int fileId);

    String readFile(int fileId);

    List<String> openAndReadFile(String path);
}
