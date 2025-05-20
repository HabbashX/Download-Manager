package com.habbashx.exception;

/**
 * This exception represents an invalid URL provided for processing.
 * It is used to indicate that a URL string does not conform to expected standards or specifications.
 *
 * The exception is typically thrown when a URL fails validation, such as during
 * the process of downloading a file or performing operations that require a valid URL format.
 *
 * For instance:
 *  - A URL not adhering to a valid HTTP, HTTPS, or FTP format.
 *  - A URL string formatting issue.
 *
 * Extends {@link RuntimeException}, making it an unchecked exception.
 *
 * Constructor:
 * - {@link #InvalidURLException(String)}: Initializes the exception with details about the invalid URL.
 */
public class InvalidURLException extends RuntimeException{

    public InvalidURLException(String message){
        super(message);
    }
}
