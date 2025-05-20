package com.habbashx.manager;

import com.habbashx.config.DownloadManagerConfig;
import com.habbashx.logger.FileLogger;
import com.habbashx.logger.Logger;
import com.habbashx.system.StorageType;
import com.habbashx.system.StorageTypeChecker;
import lombok.Getter;

/**
 * Represents an abstract task for managing the download of files. The class serves as a blueprint for handling
 * file downloads and provides functionality for initializing configurations, logging events, managing storage types,
 * and controlling download processes such as pausing, resuming, and stopping.
 *
 * This abstract class should be extended to create specific download task implementations, such as single-file
 * or parallel downloads.
 *
 * Fields include a configuration object, storage type information, connection settings, logging tools,
 * and animation settings, which support the customization and management of download processes. Subclasses are
 * expected to implement core download-related methods to define specific behaviors.
 */
@Getter
public abstract class DownloadTask {

    private final int connectionTimeout;
    private final int speedLimit;

    /**
     * The configuration object for managing settings and properties related to the download process.
     * This {@link DownloadManagerConfig} instance is used to fetch and manage configurable parameters
     * like connection timeout, download speed limits, animation settings, and download methodology.
     *
     * It provides mechanisms to load and access application-specific configuration properties
     * from a persistent storage file, ensuring that all necessary configurations are uniformly available
     * throughout the lifecycle of a download task.
     *
     * The object is immutable, and its properties are initialized at construction time, ensuring thread-safety
     * and consistency during concurrent download operations.
     */
    private final DownloadManagerConfig config;

    /**
     * Represents the type of storage medium used for managing files in the download task.
     * The storage type defines whether the files are stored on a particular type of hardware, such as an SSD or HDD.
     *
     * This variable is used in the download management process to determine and optimize
     * operations based on the underlying storage device's characteristics, such as speed and capacity.
     *
     * The value of this field is determined during the initialization of a {@code DownloadTask} instance
     * and is obtained from the {@link StorageTypeChecker#getStorageType()} method. It is immutable and
     * cannot be changed after the object is constructed.
     */
    private final StorageType storageType;

    private final String animation;

    private final FileLogger fileLogger;
    private final Logger logger;

    protected DownloadTask() {
        this.config = new DownloadManagerConfig();
        this.fileLogger = new FileLogger();
        this.logger = new Logger();
        this.connectionTimeout = (int) config.getPropertyValue("dm.settings.timeout");
        this.speedLimit = (int) config.getPropertyValue("dm.settings.speedLimit");
        this.storageType = new StorageTypeChecker().getStorageType();
        this.animation = (String) config.getPropertyValue("dm.settings.progress.animation");
    }

    public abstract void downloadFile(String urlString);
    public abstract void resumeDownload(String urlString);
    public abstract void pauseDownload();
    public abstract void stopDownload();
    public abstract void resumeFailureDownload(String urlString ,String fileDestination,int retryCount);

    public void requireNonNull(String string, String message){
        if (string.isEmpty() || string.isBlank() || string.equals("null")){
            throw new IllegalArgumentException(message);
        }
    }

    public void initializeShutDownHookOperation(Runnable runnable){
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }

    public Logger getLogger() {
        return logger;
    }

    public FileLogger getFileLogger() {
        return fileLogger;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public String getAnimation() {
        return animation;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }
}
