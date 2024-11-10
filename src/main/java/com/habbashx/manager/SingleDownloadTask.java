package com.habbashx.manager;


import com.habbashx.exception.NoSuchAnimationException;
import com.habbashx.animation.ProgressAnimation;
import com.habbashx.animation.ProgressAnimationFactory;
import com.habbashx.logger.FileLogger;
import com.habbashx.logger.Logger;

import com.habbashx.exception.InvalidURLException;

import com.habbashx.manager.buffer.BufferSize;
import com.habbashx.manager.urlvalidation.URLValidation;

import lombok.Cleanup;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.BufferedInputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.habbashx.manager.destinationOrganizer.DestinationOrganizer.organizeFileDestination;
import static com.habbashx.notification.DownloadManagerNotification.pushNotification;

import static java.awt.TrayIcon.MessageType.INFO;
import static java.awt.TrayIcon.MessageType.ERROR;

class SingleDownloadTask extends DownloadTask {

    private volatile boolean isPaused;
    private volatile boolean isStopped;

    private final FileLogger fileLogger;
    private final Logger logger;

    private static final int RETRIES = 5;

    private final ProgressAnimation animation;

    private Path path;

    private int retryCount;

    public SingleDownloadTask() throws NoSuchAnimationException {
        super();
        fileLogger = getFileLogger();
        logger = getLogger();
        animation = ProgressAnimationFactory.getInstance(getAnimation());
    }

    @Override
    public void downloadFile(String linkURL) {

        requireNonNull(linkURL,"linkURL is null or empty");

        if (!URLValidation.isValidURL(linkURL)) {
            throw new InvalidURLException(linkURL);
        }
        try {
            URL url = new URL(linkURL);

            Path path = organizeFileDestination(linkURL);
            this.path = path;
            File file = path.toFile();

            long existingFileSize = file.exists() ? file.length() : 0;
            long downloadedBytes = existingFileSize;

            int bufferSize = new BufferSize(getStorageType()).getRecommendedBufferSize();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(getConnectionTimeout());

            int fileSize = connection.getContentLength();
            if (existingFileSize > 0) {
                connection.setRequestProperty("Range", "bytes=" + existingFileSize + "-");
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {

                @Cleanup
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                @Cleanup
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.seek(downloadedBytes);
                initializeShutDownHookOperation(this::cleanupOnExit);

                int bytesDownloadedLastSecond = 0;
                long previousTime = System.currentTimeMillis();

                final int barLength = 50;

                byte[] buffer = new byte[bufferSize];
                int byteRead;
                long totalBytesRead = 0;
                long sessionStart = System.currentTimeMillis();

                while ((byteRead = inputStream.read(buffer)) != -1) {
                    if (isPaused) {
                        inputStream.close();
                        raf.close();
                    }

                    if (isStopped) {
                        inputStream.close();
                        raf.close();
                        Files.delete(path);
                        break;
                    }

                    downloadedBytes += byteRead;
                    bytesDownloadedLastSecond += byteRead;
                    raf.write(buffer, 0, byteRead);
                    totalBytesRead += byteRead;

                    long currentTime = System.currentTimeMillis();

                    if (currentTime - previousTime >= 1000) {
                        int internetSpeed = (int) (bytesDownloadedLastSecond / 1024.0);
                        int progress = (int) ((totalBytesRead * 100) / fileSize);
                        animation.printProgressBar(progress, barLength, internetSpeed, downloadedBytes,fileSize);
                        bytesDownloadedLastSecond = 0;
                    }

                }
                long sessionEnd = (System.currentTimeMillis() - sessionStart) / 1000;
                pushNotification("Download successfully", logger, INFO);
                fileLogger.logSuccess("Download successfully",linkURL);
                logger.info("Session Time: " + sessionEnd);
            } else {
                logger.warning("download failed responseCode: " + responseCode);
            }

        } catch (IOException e) {
            retryCount++;
            logger.error(e.getMessage() + " something went wrong check your internet connection\n");
            resumeFailureDownload(linkURL,path.toString(),retryCount);
        }

    }


    @Override
    public void resumeDownload(String urlString) {
        Thread downloadThread = new Thread(() -> {
            isPaused = false;
            downloadFile(urlString);
        });

        downloadThread.start();
    }

    @Override
    public void pauseDownload() {
        isPaused = true;
    }

    @Override
    public void stopDownload() {
        isStopped = true;
    }

    @Override
    public void resumeFailureDownload(String urlString, String fileDestination, int retries) {

        if (retries <= RETRIES) {
            waitRetry();
            downloadFile(urlString);
        } else {
            logger.warning("download retries reach the limit\n");
            logger.warning("download failed try again or check your internet connection\n");
            fileLogger.logFailure("Download failed",urlString);
            pushNotification("Download failed", logger,ERROR);
        }
    }

    private void waitRetry() {
        try {
            Thread.sleep(5000); // check connection every 5 second
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanupOnExit(){
        if (!isStopped){
            isStopped = true;
        }
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
