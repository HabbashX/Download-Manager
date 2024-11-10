package com.habbashx.system;

import com.habbashx.exception.UnSupportedOperatingSystem;

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
