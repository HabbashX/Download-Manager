package com.habbashx.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StorageTypeChecker {

    private final OperatingSystemChecker osChecker;

    private static final String[] WINDOWS = {"cmd", "/c", "fsutil", "behavior", "query", "disabledeletenotify"};
    private static final String[] LINUX = {"bash", "-c", "lsblk -d -o name,rota"};
    private static final String[] MAC = {"\"diskutil\", \"info\", \"diskIdentifier\""};

    public StorageTypeChecker() {
        osChecker = new OperatingSystemChecker();
    }

    public StorageType getStorageType() {

        OperatingSystemType osType = osChecker.getOSName();

        return switch (osType) {
            case WINDOWS -> getOSDriveType(WINDOWS);
            case LINUX -> getOSDriveType(LINUX);
            case MACOS -> getOSDriveType(MAC);
        };
    }


    private StorageType getOSDriveType(String[] command) {

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null){
                if (line.contains("DisableDeleteNotify = 0") || line.contains("0") || line.contains("Solid State: Yes")) {
                    return StorageType.SSD;
                } else {
                    return StorageType.HDD;
                }
            }
            process.waitFor();
        }catch (IOException | InterruptedException e){
            throw new RuntimeException(e);
        }
        return null;
    }
}
