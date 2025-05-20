package com.habbashx.animation;

/**
 * Abstract base class for implementing various progress animations. This class defines a
 * contract for creating custom progress bars that visualize download progress, internet
 * speed, and other details.
 *
 * Implementers are expected to provide an implementation for the {@code printProgressBar}
 * method, enabling customized progress visualization.
 */
public abstract class ProgressAnimation {

    protected static final int MEGA_BYTES = 1024;
    protected static final int GIGA_BYTES = 1048576;

    /**
     * Displays a progress bar visualization during an ongoing operation such as file download.
     *
     * @param progress        the current progress percentage, ranging from 0 to 100.
     * @param barLength       the length of the progress bar to be displayed, typically in characters.
     * @param internetSpeed   the current internet speed in kilobytes per second (KB/s).
     * @param downloadedBytes the total number of bytes that have been downloaded so far.
     * @param fileSize        the total size of the file being downloaded, in bytes.
     */
    public abstract void printProgressBar(int progress, int barLength, double internetSpeed, long downloadedBytes ,int fileSize);

}
