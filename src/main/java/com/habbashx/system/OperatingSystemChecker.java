package com.habbashx.system;

import com.habbashx.exception.UnSupportedOperatingSystem;

/**
 * The OperatingSystemChecker class is responsible for determining the operating system
 * of the host machine. It provides functionality to identify the type of operating
 * system (e.g., Windows, Linux, macOS) based on system properties.
 *
 * Methods:
 * - getOSName(): Identifies the current operating system and returns the corresponding
 *   OperatingSystemType enum value. If the operating system is not supported, this method
 *   throws an UnSupportedOperatingSystem exception.
 *
 * Usage:
 * This class can be used as a utility to perform OS-specific operations based on the
 * detected operating system type.
 *
 * Exceptions:
 * - UnSupportedOperatingSystem: Thrown when the operating system is not recognized or
 *   supported by the application.
 */
class OperatingSystemChecker {

    public OperatingSystemType getOSName(){
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")){
            return OperatingSystemType.WINDOWS;
        } else if (osName.contains("linux")) {
            return OperatingSystemType.LINUX;
        } else if (osName.contains("mac")) {
            return OperatingSystemType.MACOS;
        } else {
            throw new UnSupportedOperatingSystem();
        }
    }
}
