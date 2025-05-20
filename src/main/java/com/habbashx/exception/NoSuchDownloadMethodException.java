package com.habbashx.exception;

/**
 * This exception is thrown to indicate that a specified download method is not recognized
 * or supported by the system. It serves as a custom exception to handle invalid download
 * method scenarios.
 *
 * Typically, this exception might be used in a factory or a configuration class, where the
 * application checks for supported download methods (e.g., "single" or "parallel") and
 * throws this exception if an unrecognized method is provided.
 *
 * Use this exception to explicitly signal an unsupported download method rather than relying
 * on more generic alternatives such as {@code IllegalArgumentException}.
 */
public class NoSuchDownloadMethodException extends Exception {
    public NoSuchDownloadMethodException(String message) {
        super(message);
    }
}
