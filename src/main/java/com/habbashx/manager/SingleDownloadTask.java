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

/**
 * The `SingleDownloadTask` class represents a concrete implementation of the `DownloadTask` abstract class
 * to handle single file downloads. It provides core functionality such as downloading, pausing, stopping,
 * resuming a file download, and handling retry logic for failed downloads.
 *
 * This class is designed to operate as a single-threaded file download utility with logging, progress bar
 * animations, and error management capabilities.
 *
 * Key Features:
 * - Implements the file download process with options to resume or retry upon failures.
 * - Supports pausing and stopping downloads.
 * - Manages and logs download success and failure events using `FileLogger` and standard logging mechanisms.
 * - Dynamically displays download progress using a customizable animation style via `ProgressAnimation`.
 * - Handles partial downloads using "Range" HTTP headers.
 * - Implements retry logic with a predefined limit for failed downloads.
 *
 * Fields:
 * - `isPaused`: Indicates whether the download is paused. Used for pausing mechanism.
 * - `isStopped`: Indicates whether the download is stopped. Used for stopping mechanism.
 * - `fileLogger`: Instance of `FileLogger` to record download events.
 * - `logger`: Logger instance for logging general events.
 * - `RETRIES`: Static variable that defines the maximum retry limit for failed download attempts.
 * - `animation`: Instance of `ProgressAnimation` used for rendering the download progress bar.
 * - `path`: Represents the file destination path for the downloaded file.
 * - `retryCount`: Keeps track of the current retry attempt count.
 *
 * Constructor:
 * - `SingleDownloadTask()`:
 *   Initializes the class, retrieves logging instances, configures download animation, and sets up required resources.
 *   Throws `NoSuchAnimationException` if the specified animation type is not found.
 *
 * Methods:
 * - `downloadFile(String linkURL)`:
 *   Downloads the provided file from the specified URL, with support for partial downloads, progress tracking,
 *   and retry handling. Validates the input URL and organizes the file destination for the download. Handles
 *   possible I/O exceptions and notifies the user on success or failure.
 *
 * - `resumeDownload(String urlString)`:
 *   Resumes a paused or partially downloaded file by spinning up a new thread to initiate the download process.
 *
 * - `pauseDownload()`:
 *   Pauses an ongoing download. The current state of the download is maintained for resumption.
 *
 * - `stopDownload()`:
 *   Stops the ongoing download entirely. Deletes the partially downloaded file and releases open resources.
 *
 * - `resumeFailureDownload(String urlString, String fileDestination, int retries)`:
 *   Handles retry logic for downloads that failed. If the retry count does not exceed the predefined
 *   limit (`RETRIES`), it waits for a fixed time period before retrying the download.
 *   Logs a failure message and notifies the user upon exceeding retry attempts.
 *
 * Private Methods:
 * - `waitRetry()`:
 *   Waits for a specified waiting time before retrying a failed download. Used in retry logic to handle
 *   intermittent network issues.
 *
 * - `cleanupOnExit()`:
 *   Ensures that resources are cleaned up in case of unexpected program termination or after stopping a download.
 *   Deletes the partially downloaded file to prevent corruption or mismanagement.
 */
class SingleDownloadTask extends DownloadTask {

    /**
     * Represents the current paused state of the download task execution.
     *
     * This volatile flag is used to control the pausing and resuming behavior
     * of the `SingleDownloadTask`. When set to `true`, the download operation
     * halts until it is resumed. Ensures thread-safety in a concurrent environment
     * by declaring the field as `volatile`.
     */
    private volatile boolean isPaused;
    /**
     * A flag indicating whether the download task has been stopped.
     *
     * This variable is used to track the state of the task and ensure
     * proper management and termination of the download process. Once set to true,
     * it signifies that the task is no longer active and further operations related
     * to the task should cease.
     *
     * The `volatile` modifier ensures that changes to this variable
     * are immediately visible to all threads, providing thread-safety
     * in concurrent environments.
     */
    private volatile boolean isStopped;

    private final FileLogger fileLogger;
    private final Logger logger;

    /**
     * The maximum number of retry attempts allowed for a failed download task.
     * This constant is used in scenarios where a download operation fails
     * and needs to be retried. It ensures that the retry mechanism does not
     * exceed the defined limit to avoid infinite loops or excessive retries.
     *
     * This value can be utilized in the context of controlling failure recovery
     * strategies, enabling robust handling of transient issues such as network
     * instability or server unavailability.
     */
    private static final int RETRIES = 5;

    /**
     * A variable representing a progress animation used to visually indicate the
     * progression of a task or an operation. The animation's behavior and state
     * are controlled by the associated logic within the application.
     *
     * This variable is immutable and ensures that the animation state cannot
     * be altered once initialized. It is intended to provide encapsulated access
     * to the animation mechanism for status representation.
     */
    private final ProgressAnimation animation;

    private Path path;

    private int retryCount;

    public SingleDownloadTask() throws NoSuchAnimationException {
        super();
        fileLogger = getFileLogger();
        logger = getLogger();
        animation = ProgressAnimationFactory.getInstance(getAnimation());
    }

    /**
     * Downloads a file from the specified URL.
     *
     * @param linkURL the URL of the file to be downloaded. Must be a valid HTTP/HTTPS/FTP URL.
     *                Throws IllegalArgumentException if null or empty.
     *                Throws InvalidURLException if the URL is invalid.
     */
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


    /**
     * Resumes a previously paused file download operation for the specified URL.
     * This method restarts the download in a new thread by setting the pause flag to false
     * and invoking the file download logic.
     *
     * @param urlString the URL of the file to resume downloading. Must be a valid HTTP/HTTPS/FTP URL.
     *                  Throws IllegalArgumentException if null or empty.
     */
    @Override
    public void resumeDownload(String urlString) {
        Thread downloadThread = new Thread(() -> {
            isPaused = false;
            downloadFile(urlString);
        });

        downloadThread.start();
    }

    /**
     * Pauses the ongoing download process by setting the pause state to true.
     * This method updates the internal state to indicate that the download should
     * no longer proceed until manually resumed.
     */
    @Override
    public void pauseDownload() {
        isPaused = true;
    }

    /**
     * Stops the ongoing download process by setting the internal state to indicate that
     * the download operation should terminate. This method updates the download status
     * to a "stopped" state, preventing further progress.
     *
     * This operation can interrupt an active download, requiring any associated threading
     * or resource allocation to cease productive operations tied to the download task.
     */
    @Override
    public void stopDownload() {
        isStopped = true;
    }

    /**
     * Attempts to resume a failed download for the given URL and persists the download to a specified file destination.
     * The method retries downloading based on a limited number of attempts defined by the RETRIES field.
     * If the retry limit is reached, an appropriate log message is recorded and a failure notification is sent.
     *
     * @param urlString        the URL of the file to resume downloading. Must be a valid HTTP/HTTPS/FTP URL.
     *                         Throws IllegalArgumentException if null or empty.
     * @param fileDestination  the destination path where the file will be saved during the download.
     *                         Should be a valid writable file path.
     * @param retries          the current retry attempt count. If it exceeds the RETRIES limit, the download will fail.
     */
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

    /**
     * Causes the current thread to pause execution for a fixed duration of time
     * before retrying an operation, allowing for periodic checks or actions.
     *
     * This method halts the thread for 5 seconds using {@code Thread.sleep},
     * and in case of an {@code InterruptedException}, it propagates the exception
     * by wrapping it in a {@code RuntimeException}.
     *
     * Throws:
     * - RuntimeException: If the thread is interrupted while sleeping, the exception is rethrown as a runtime exception.
     */
    private void waitRetry() {
        try {
            Thread.sleep(5000); // check connection every 5 second
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cleans up resources and performs necessary operations when the task is exiting.
     *
     * This method ensures that the task has been stopped by toggling the `isStopped`
     * flag to true, preventing further operations. It then attempts to delete the file
     * specified by the `path` field if it exists.
     *
     * Exceptions:
     * - If an {@code IOException} occurs during file deletion, it is propagated
     *   as a {@code RuntimeException}.
     */
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
