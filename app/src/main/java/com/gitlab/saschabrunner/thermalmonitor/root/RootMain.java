package com.gitlab.saschabrunner.thermalmonitor.root;

import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.BuildConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.librootjava.RootIPC;
import eu.chainfire.librootjava.RootJava;

public class RootMain {
    private static final String TAG = "RootMain";

    public static void main(String[] args) {
        RootJava.restoreOriginalLdLibraryPath();

        IBinder ipc = new IIPC.Stub() {
            private List<FileChannel> openFiles = new ArrayList<>();
            private ByteBuffer fileChannelReadBuffer;
            private final Object readBufferLock = new Object();

            @Override
            public List<String> listFiles(String path) {
                File dir = new File(path);
                File[] files = dir.listFiles();

                List<String> out = new ArrayList<>(files.length);
                for (File file : files) {
                    out.add(file.getAbsolutePath());
                }
                return out;
            }

            @Override
            public int openFile(String path, int maxLength) throws RemoteException {
                if (maxLength <= 0) {
                    throw new RemoteException("Parameter 'maxLength' must be positive");
                }

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        openFiles.add(FileChannel.open(Paths.get(path), StandardOpenOption.READ));
                    } else {
                        FileInputStream stream = new FileInputStream(path);
                        openFiles.add(stream.getChannel());
                    }
                } catch (IOException e) {
                    String msg = "Couldn't open file " + path;
                    Log.e(TAG, msg, e);
                    throw new RemoteException(msg);
                }

                ensureBufferSize(maxLength);

                return openFiles.size() - 1;
            }

            @Override
            public boolean closeFile(int fileId) throws RemoteException {
                if (fileId < 0 || fileId >= openFiles.size()) {
                    // ID does not refer to a open file
                    return false;
                }

                try {
                    openFiles.get(fileId).close();
                } catch (IOException e) {
                    String msg = "Couldn't close file " + fileId;
                    Log.e(TAG, msg, e);
                    throw new RemoteException(msg);
                }

                return true;
            }

            private void ensureBufferSize(int expectedSize) {
                synchronized (readBufferLock) {
                    if (fileChannelReadBuffer == null
                            || fileChannelReadBuffer.capacity() < expectedSize) {
                        fileChannelReadBuffer = ByteBuffer.allocate(expectedSize);
                    }
                }
            }

            @Override
            public String readFile(int fileId) throws RemoteException {
                if (fileId < 0 || fileId >= openFiles.size()) {
                    throw new RemoteException("File ID " + fileId + " is not valid");
                }

                synchronized (readBufferLock) {
                    try {
                        FileChannel file = openFiles.get(fileId);

                        // Reset file channel and buffer to beginning
                        file.position(0);
                        fileChannelReadBuffer.position(0);

                        // Read file into buffer
                        int length = file.read(fileChannelReadBuffer);

                        // Parse integer value
                        return new String(fileChannelReadBuffer.array(), 0, length - 1);
                    } catch (IOException e) {
                        String msg = "Couldn't read file " + fileId;
                        Log.e(TAG, msg, e);
                        throw new RemoteException(msg);
                    }
                }
            }

            @Override
            public List<String> openAndReadFile(String path) throws RemoteException {
                ArrayList<String> out = new ArrayList<>();

                try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                    while (true) {
                        String curLine = reader.readLine();
                        if (curLine == null) {
                            break;
                        }

                        out.add(curLine);
                    }
                } catch (IOException e) {
                    String msg = "Couldn't read file " + path;
                    Log.e(TAG, msg, e);
                    throw new RemoteException(msg);
                }

                return out;
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
}
