package com.habbashx.manager;

import com.habbashx.exception.NoSuchAnimationException;
import com.habbashx.exception.NoSuchDownloadMethodException;

/**
 * Factory class for creating instances of {@link DownloadTask}.
 * This class provides a static method to retrieve a specific implementation
 * of the {@code DownloadTask} based on the provided download method.
 */
public class DownloadTaskFactory {

    /**
     * Returns an instance of a specific DownloadTask implementation based on the provided download method.
     * It supports creating tasks for single file downloads and parallel file downloads.
     *
     * @param downloadMethod the method of download. Acceptable values are:
     *                       - "single" for a single-file download task
     *                       - "parallel" for a parallel download task
     * @return an instance of a DownloadTask implementation corresponding to the specified download method
     * @throws NoSuchAnimationException if the associated animation for the download task cannot be found
     * @throws NoSuchDownloadMethodException if the specified download method is not recognized
     */
    public static DownloadTask getInstance(String downloadMethod) throws NoSuchAnimationException, NoSuchDownloadMethodException {

       return switch (downloadMethod) {
            case "single" -> new SingleDownloadTask();
            case "parallel" -> new ParallelDownloadTask();
           default -> throw new NoSuchDownloadMethodException("no such download method: " + downloadMethod);
       };
    }
}
