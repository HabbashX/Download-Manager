package com.habbashx.exception;

/**
 * The UnSupportedSystemTray class represents a runtime exception that is thrown
 * when an attempt is made to use the system tray functionality on a platform
 * that does not support it.
 *
 * This exception is used to indicate platform-level constraints where the
 * required system tray functionality is unavailable. It should be caught and
 * handled appropriately to avoid application crashes in unsupported environments.
 */
public class UnSupportedSystemTray extends RuntimeException {
    public UnSupportedSystemTray(String message) {
        super(message);
    }
}
