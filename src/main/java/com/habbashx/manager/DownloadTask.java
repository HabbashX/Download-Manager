package com.habbashx.manager;

import com.habbashx.config.DownloadManagerConfig;
import com.habbashx.logger.FileLogger;
import com.habbashx.logger.Logger;
import com.habbashx.system.StorageType;
import com.habbashx.system.StorageTypeChecker;
import lombok.Getter;

@Getter
public abstract class DownloadTask {

    private final int connectionTimeout;
    private final int speedLimit;

    private final DownloadManagerConfig config;

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

}
