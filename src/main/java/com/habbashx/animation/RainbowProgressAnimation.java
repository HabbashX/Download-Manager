package com.habbashx.animation;

/**
 * Represents a progress animation for displaying download or task progress with a colorful
 * rainbow-style visualization. This class is an extension of the abstract {@code ProgressAnimation}
 * class and is intended to provide a vibrant, visually dynamic progress bar.
 *
 * Note: This class is currently a work in progress and does not yet provide a specific implementation
 * for the {@code printProgressBar} method. In future versions, this method is expected to deliver a
 * colorful progress bar.
 *
 * Users attempting to utilize this animation type via the {@code ProgressAnimationFactory} should ensure
 * that they account for its incomplete implementation.
 */
class RainbowProgressAnimation extends ProgressAnimation {

    @Override
    public void printProgressBar(int progress, int barLength, double internetSpeed, long downloadedBytes,int fileSize) {
        System.out.println("coming soon");
    }
}
