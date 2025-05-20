package com.habbashx.exception;

/**
 * Indicates that the operating system being used is not supported by the application.
 *
 * This exception is thrown when an attempt is made to identify the operating system,
 * but the detected operating system does not match any of the supported types
 * (e.g., Windows, Linux, or macOS).
 *
 * Common usage includes throwing this exception in scenarios where the application
 * cannot proceed due to an unrecognized or unsupported operating system environment.
 *
 * This exception extends RuntimeException, meaning it is unchecked and does not
 * require explicit declaration in method signatures.
 */
public class UnSupportedOperatingSystem extends RuntimeException {

    public UnSupportedOperatingSystem() {
    }
}
