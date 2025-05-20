package com.habbashx.animation;

import com.habbashx.exception.NoSuchAnimationException;

/**
 * Factory class responsible for creating instances of various progress animation types.
 * This class provides a mechanism to dynamically create and return an implementation
 * of {@code ProgressAnimation} based on a specified animation type.
 *
 * The supported animation types are:
 * - "default": {@code DefaultProgressAnimation}, a basic progress bar implementation.
 * - "rainbow": {@code RainbowProgressAnimation}, a colorful progress bar (work in progress).
 * - "arrow": {@code ArrowProgressAnimation}, a progress bar with an arrow indicating the current progress.
 *
 * Unsupported animation types will result in a {@code NoSuchAnimationException} being thrown.
 */
public class ProgressAnimationFactory {

    public static ProgressAnimation getInstance(String animation) throws NoSuchAnimationException {

        return switch (animation){
            case "default" -> new DefaultProgressAnimation();
            case "rainbow" -> new RainbowProgressAnimation();
            case "arrow" -> new ArrowProgressAnimation();
            default -> throw new NoSuchAnimationException("No such animation: " + animation);
        };
    }
}
