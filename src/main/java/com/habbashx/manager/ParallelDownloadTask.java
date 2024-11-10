package com.habbashx.manager;

import com.habbashx.exception.InvalidURLException;
import com.habbashx.exception.NoSuchAnimationException;

import com.habbashx.animation.ProgressAnimation;
import com.habbashx.animation.ProgressAnimationFactory;

import com.habbashx.logger.FileLogger;
import com.habbashx.logger.Logger;

import com.habbashx.manager.buffer.BufferSize;
import com.habbashx.manager.urlvalidation.URLValidation;

import java.io.IOException;
import java.io.InputStream;

import java.io.RandomAccessFile;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.habbashx.manager.destinationOrganizer.DestinationOrganizer.organizeFileDestination;
import static com.habbashx.notification.DownloadManagerNotification.pushNotification;

import static java.awt.TrayIcon.MessageType.INFO;
import static java.awt.TrayIcon.MessageType.ERROR;

import static java.util.concurrent.Executors.newFixedThreadPool;

class ParallelDownloadTask extends DownloadTask {

    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;

    private final AtomicLong totalDownloadedBytes = new AtomicLong(0);

    private static final int THREAD_COUNT = 4;

    private final ExecutorService executorService = newFixedThreadPool(THREAD_COUNT);

    private final Logger logger;
    private final FileLogger fileLogger;

    private final ProgressAnimation animation;

    private final AtomicInteger finishedChunk = new AtomicInteger(0);

    private Path path;

    public ParallelDownloadTask() throws NoSuchAnimationException {
        super();
        fileLogger = getFileLogger();
        logger = getLogger();
        animation = ProgressAnimationFactory.getInstance(getAnimation());
    }

    @Override
    public void downloadFile(String linkURL) {
        initializeShutDownHookOperation(this::cleanupOnExit);
        requireNonNull(linkURL, "linkURL is null or empty");

        if (!URLValidation.isValidURL(linkURL)) {
            throw new InvalidURLException(linkURL);
        }

        try {
            URL url = new URL(linkURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            Path fileDestination = organizeFileDestination(linkURL);
            path = fileDestination;
            int responseCode = connection.getResponseCode();

            if (Files.exists(fileDestination)) {
                logger.warning("file already exists with this name");
            }

            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(getConnectionTimeout());

            int fileSize = connection.getContentLength();
            int chunkSize = fileSize / THREAD_COUNT;

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {

                for (int i = 0; i < THREAD_COUNT; i++) {
                    int start = i * chunkSize;
                    int end = (i == THREAD_COUNT - 1) ? fileSize : start + chunkSize - 1;
                    executorService.submit(new ChunkDownloadTask(linkURL, fileDestination.toString(), start, end));
                }

                long previousTime = System.currentTimeMillis();
                long lastDownloadedBytes = 0;
                long sessionStart = System.currentTimeMillis();

                executorService.shutdown();
                while (!executorService.isTerminated()) {
                    synchronized (this) {
                        if (isPaused) {
                            wait();
                        }
                    }

                    long currentTime = System.currentTimeMillis();

                    if (currentTime - previousTime >= 1000) {
                        double internetSpeed = (totalDownloadedBytes.get() - lastDownloadedBytes) / 1024.0;

                        int progress = (int) ((totalDownloadedBytes.get() * 100) / fileSize);

                        animation.printProgressBar(progress, 50, internetSpeed, totalDownloadedBytes.get(), fileSize);
                        previousTime = currentTime;
                        lastDownloadedBytes = totalDownloadedBytes.get();
                    }
                }

                notifyUser(sessionStart, linkURL);

            } else {
                logger.warning("download failed responseCode: " + responseCode);
            }

        } catch(InterruptedException | IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resumeDownload(String urlString) {
        isPaused = false;
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public void pauseDownload() {
        isPaused = true;
    }

    @Override
    public void stopDownload() {
        isStopped = true;
        isPaused = false;
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    @Deprecated(since = "1.0-alpha")
    public void resumeFailureDownload(String urlString, String fileDestination, int retries) {}

    private void notifyUser(long session ,String linkURL){
        if (finishedChunk.get() > 3) {
            long sessionEnd = (System.currentTimeMillis() - session) / 1000;
            pushNotification("Download Successfully", logger, INFO);
            fileLogger.logSuccess("Download successfully", linkURL);
            logger.info("session time: " + sessionEnd + "s");
        } else {
            logger.error("something went wrong please check your internet connection and try again");
            fileLogger.logFailure("Download failed connection lost",linkURL);
            pushNotification("Download Failed",logger,ERROR);
        }
    }

    private void cleanupOnExit(){
        if (!isStopped){
            isStopped = true;
            executorService.shutdownNow();
        }
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ChunkDownloadTask implements Runnable {

        private final String urlString;
        private final String fileDestination;
        private final int start;
        private final int end;

        public ChunkDownloadTask(String urlString, String fileDestination, int start, int end) {
            this.urlString = urlString;
            this.fileDestination = fileDestination;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);

                int bufferSize = new BufferSize(getStorageType()).getRecommendedBufferSize();
                int currentPosition = start;

                InputStream inputStream = connection.getInputStream();
                connection.setConnectTimeout(getConnectionTimeout());
                RandomAccessFile raf = new RandomAccessFile(fileDestination,"rw");
                raf.seek(currentPosition);
                raf.getChannel().force(true);

                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    synchronized (ParallelDownloadTask.this) {
                        while (isPaused) {
                            wait();
                        }
                        if (isStopped) {
                            inputStream.close();
                            raf.close();
                            connection.disconnect();
                            break;
                        }
                    }

                    raf.write(buffer, 0, bytesRead);
                    currentPosition += bytesRead;
                    totalDownloadedBytes.addAndGet(bytesRead);
                }
                finishedChunk.addAndGet(1);
                inputStream.close();
                raf.close();
                connection.disconnect();
            } catch (IOException e) {
               finishedChunk.addAndGet(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
