package com.habbashx.animation;

import com.habbashx.exception.NoSuchAnimationException;

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
