package com.habbashx.exception;

/**
 * Exception thrown when a specified animation type is not recognized or does not exist.
 *
 * This exception is primarily used in scenarios where an invalid or unsupported animation
 * type is requested from components such as the {@code ProgressAnimationFactory}.
 *
 * Typical scenarios include:
 * - A request for an animation type that is not implemented or registered.
 * - Incorrect configuration or usage in components requiring an {@code Animation} instance.
 *
 * This exception includes a message indicating the specific animation type that was not found.
 */
public class NoSuchAnimationException extends Exception{

    public NoSuchAnimationException(String message){
        super(message);
    }
}
