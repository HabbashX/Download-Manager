package com.habbashx.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The StorageTypeChecker class determines the type of storage device (SSD or HDD) used
 * in the current system. It utilizes OS-specific commands to identify the underlying
 * storage type based on the operating system of the host machine.
 *
 * The class delegates operating system identification to the OperatingSystemChecker and
 * executes respective command-line instructions to gather information about the storage.
 * The results of the commands are processed to classify the storage type.
 *
 * This class relies on the following key components:
 * - OperatingSystemChecker: Determines the operating system of the host machine.
 * - StorageType: Represents the storage type (SSD or HDD) determined from the command output.
 *
 * Methods:
 * - getStorageType(): Identifies the current operating system, executes the corresponding
 *   command-line instructions, and determines whether the primary storage is SSD or HDD.
 *
 * This class supports the following operating systems:
 * - Windows
 * - Linux
 * - macOS
 *
 * Exceptions:
 * - RuntimeException: Thrown in case of any IO or process-related errors during command execution.
 */
public class StorageTypeChecker {

    private final OperatingSystemChecker osChecker;

    /**
     * An array of strings representing the command-line instruction to query the storage
     * device type on Windows operating systems. This command specifically checks the
     * value of "DisableDeleteNotify" to determine if the storage device is an SSD or HDD.
     *
     * Elements:
     * - "cmd": Specifies the Windows command-line interpreter.
     * - "/c": Indicates that the following string is a command to be executed.
     * - "fsutil": A utility that provides file and volume management features.
     * - "behavior": A subcommand to manage behaviors or operations.
     * - "query": Used to request specific data.
     * - "disabledeletenotify": A parameter used to check the status of TRIM support,
     *   which is typically associated with SSDs.
     *
     * This constant is utilized in the {@link StorageTypeChecker} class when the
     * operating system is recognized as Windows. The command helps in identifying
     * whether the storage device uses SSD or HDD technology by interpreting the
     * output of the executed command.
     */
    private static final String[] WINDOWS = {"cmd", "/c", "fsutil", "behavior", "query", "disabledeletenotify"};
    /**
     * A constant array representing a command to be executed on Linux systems.
     * The command consists of using "bash" with the "-c" option to execute a system-level
     * command. Specifically, it lists block devices along with their rotational property.
     *
     * The output of this command includes:
     * - `name`: The name of the block device.
     * - `rota`: A flag indicating the rotational status of the device.
     */
    private static final String[] LINUX = {"bash", "-c", "lsblk -d -o name,rota"};
    /**
     * An array of command strings used to query storage device information specific to macOS.
     *
     * This constant is utilized within the StorageTypeChecker class to execute macOS-specific
     * commands for determining the type of storage device. The commands are designed to interact
     * with the macOS disk utility, retrieving information such as disk identifiers and
     * other storage attributes.
     *
     * The commands included in this array are:
     * - "diskutil": A macOS command-line utility for managing disk drives and volumes.
     * - "info": A subcommand of diskutil to retrieve detailed information about a specified disk.
     * - "diskIdentifier": Retrieves the identifier for a specific disk on macOS.
     *
     * This array is used as input for the getOSDriveType method in the StorageTypeChecker class
     * to extract storage-related data and classify the storage type.
     */
    private static final String[] MAC = {"\"diskutil\", \"info\", \"diskIdentifier\""};

    public StorageTypeChecker() {
        osChecker = new OperatingSystemChecker();
    }

    /**
     * Determines the type of storage device (SSD or HDD) used by the operating system's primary
     * storage drive based on the type of operating system. The method identifies the current
     * operating system using the {@code osChecker} and retrieves the storage type accordingly.
     *
     * @return the type of storage as a {@code StorageType} enum
     *         ({@code StorageType.SSD} or {@code StorageType.HDD}). Returns {@code null} if the
     *         storage type cannot be determined. An exception may be thrown if the operating
     *         system is unsupported.
     */
    public StorageType getStorageType() {

        OperatingSystemType osType = osChecker.getOSName();

        return switch (osType) {
            case WINDOWS -> getOSDriveType(WINDOWS);
            case LINUX -> getOSDriveType(LINUX);
            case MACOS -> getOSDriveType(MAC);
        };
    }


    /**
     * Determines the type of the operating system's primary storage drive (SSD or HDD)
     * based on the output of a given command.
     *
     * @param command an array of strings representing the command to execute for retrieving
     *                drive information. Typically, it contains system-specific commands or scripts.
     * @return the type of the drive as a {@code StorageType} enum. Returns
     *         {@code StorageType.SSD} if the command output indicates an SSD,
     *         {@code StorageType.HDD} otherwise. Returns {@code null} if the drive type
     *         could not be determined.
     * @throws RuntimeException if an IOException or InterruptedException occurs while
     *                          executing the command.
     */
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
