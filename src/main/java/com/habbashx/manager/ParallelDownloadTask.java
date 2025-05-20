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

/**
 * Represents a task for downloading files in parallel using multiple threads.
 * This class extends the abstract {@code DownloadTask} to provide functionality for
 * downloading large files by dividing them into chunks and processing each chunk concurrently.
 *
 * The class uses a fixed thread pool to manage parallel downloads and ensures thread-safe
 * operations using synchronization and atomic variables.
 *
 * Features include:
 * - Downloading files by splitting them into a defined number of chunks.
 * - Pausing, resuming, and stopping download operations safely.
 * - Animated progress bar for monitoring download progress and speed.
 * - Logging success or failure events to console and file logs.
 * - Cleanup operations to handle interruptions or failures gracefully.
 *
 * This class employs internally defined components such as {@code ChunkDownloadTask}, which represents
 * the task of downloading a specific portion of the file, and makes use of external utilities like
 * {@code Logger} and {@code ProgressAnimation} for logging and user interaction.
 */
class ParallelDownloadTask extends DownloadTask {

    /**
     * A volatile boolean flag indicating whether the current download task is paused.
     *
     * This variable is utilized within the {@code ParallelDownloadTask} to control
     * the execution flow of download operations. When {@code true}, the task's download
     * progress is temporarily halted. Conversely, when {@code false}, the task is
     * actively downloading data.
     *
     * The use of the {@code volatile} keyword ensures visibility of updates to this variable
     * across multiple threads, as {@code ParallelDownloadTask} often operates in a
     * multithreaded environment.
     *
     * This flag is primarily modified through methods like {@code pauseDownload()} and {@code resumeDownload()},
     * and it plays a critical role in coordinating the state management of the download task.
     */
    private volatile boolean isPaused = false;
    /**
     * A flag indicating whether the process, service, or operation should be stopped.
     * This variable is declared as volatile to ensure proper visibility of its value
     * across multiple threads.
     */
    private volatile boolean isStopped = false;

    private final AtomicLong totalDownloadedBytes = new AtomicLong(0);

    /**
     * Represents the number of threads to be used for executing parallel download tasks within the
     * {@code ParallelDownloadTask} class. This constant defines the fixed number of concurrent threads
     * that the application allocates for handling download operations.
     *
     * It ensures efficient utilization of system resources by limiting the number of simultaneous
     * threads while maintaining a balance between download performance and resource constraints.
     *
     * The value is immutable and cannot be modified at runtime. It is used internally to configure
     * the thread pool for managing parallel downloads.
     */
    private static final int THREAD_COUNT = 4;

    /**
     * An {@link ExecutorService} instance used for managing a pool of worker threads.
     * The thread pool size is determined by the constant {@code THREAD_COUNT}.
     * This instance allows for asynchronous task execution, providing
     * improved performance for concurrent and parallel operations.
     *
     * The {@code executorService} ensures proper thread management, including
     * lifecycle handling and reuse of threads, to optimize resource utilization.
     * It is recommended to terminate the executor service properly
     * to release resources when it is no longer needed.
     */
    private final ExecutorService executorService = newFixedThreadPool(THREAD_COUNT);

    private final Logger logger;
    private final FileLogger fileLogger;

    /**
     * Represents an instance of {@link ProgressAnimation} used to display a progress bar visualization
     * during file download operations. The progress animation provides real-time feedback on the
     * percentage of completion, download speed, and the amount of data downloaded.
     *
     * This variable is final and cannot be modified after initialization. It is responsible
     * for invoking animated visual feedback in the context of multi-threaded downloads, ensuring
     * enhanced user experience.
     */
    private final ProgressAnimation animation;

    /**
     * Represents the count of chunks that have been successfully processed or completed during the
     * execution of a parallel download task. This variable is primarily used to track and monitor
     * the number of finished chunks within the context of a multi-threaded file download process.
     *
     * It is implemented as a thread-safe {@link AtomicInteger} to ensure proper synchronization and
     * atomic updates in a concurrent environment, where multiple threads may increment the value
     * simultaneously.
     *
     * This variable helps in determining the progress of the download operation by comparing it
     * against the total number of chunks to be downloaded. It may also be utilized for logging,
     * reporting progress, or triggering certain logic, such as notifying completion.
     */
    private final AtomicInteger finishedChunk = new AtomicInteger(0);

    private Path path;

    public ParallelDownloadTask() throws NoSuchAnimationException {
        super();
        fileLogger = getFileLogger();
        logger = getLogger();
        animation = ProgressAnimationFactory.getInstance(getAnimation());
    }

    /**
     * Downloads a file from the specified URL using a multi-threaded approach, handling partial content
     * downloads and providing a progress update with internet speed calculation. The operation supports
     * pausing, resuming, and stopping while performing cleanup at the end of execution.
     *
     * @param linkURL the URL to download the file from. It must be a valid HTTP, HTTPS, or FTP URL.
     *                Throws {@code InvalidURLException} if the URL is invalid.
     */
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

    /**
     * Resumes a previously paused download operation for the specified URL.
     *
     * This method should be called after a download has been paused using the
     * corresponding `pauseDownload` method. It resets the pause state and notifies
     * all waiting threads to continue the download process.
     *
     * @param urlString the URL of the file being downloaded. This parameter specifies
     *                  the target file location to resume the download operation from.
     */
    @Override
    public void resumeDownload(String urlString) {
        isPaused = false;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Pauses the ongoing download operation for this task. When invoked, the method sets the internal
     * state to indicate that the download is paused. The download process is expected to stop progress
     * temporarily until it is resumed using the appropriate method.
     *
     * This method is part of the implementation of the {@code DownloadTask} interface or abstract class,
     * which provides a contract for managing download lifecycle events such as pausing, resuming,
     * and stopping downloads.
     *
     * It is designed to offer users the ability to pause downloads, especially in scenarios where
     * network bandwidth needs to be prioritized for other operations or when the download temporarily
     * needs to be halted due to external factors.
     *
     * Note: Calling this method does not terminate the download but only suspends its progress.
     * Ensure that the associated resume mechanism is employed to restart the download from where it
     * was paused.
     */
    @Override
    public void pauseDownload() {
        isPaused = true;
    }

    /**
     * Stops the currently ongoing download process and signals all waiting threads.
     *
     * This method sets the `isStopped` flag to true and the `isPaused` flag to false,
     * effectively halting the download operation. Additionally, it notifies all threads
     * waiting on the associated object's monitor to ensure proper termination and cleanup.
     *
     * It should be used when the download needs to be terminated immediately and all
     * resources associated with the download process must be released.
     */
    @Override
    public void stopDownload() {
        isStopped = true;
        isPaused = false;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Attempts to resume a previously failed download operation for a specified URL.
     * This method retries downloading the file from its last known state using the given number of retry attempts.
     * It is marked as deprecated and may be removed in future versions. Consider using alternative methods for
     * download management.
     *
     * @param urlString the URL of the file that failed during download. It specifies the location of the file to resume downloading from.
     * @param fileDestination the local destination path where the downloaded file will be saved. It represents the directory location for the resumed file.
     * @param retries the number of retry attempts allowed for resuming the failed download. This parameter dictates how many times the operation will
     *                attempt to recover before failing completely.
     */
    @Override
    @Deprecated(since = "1.0")
    public void resumeFailureDownload(String urlString, String fileDestination, int retries) {}

    /**
     * Notifies the user about the download outcome based on the number of finished chunks.
     * If the download is successful, logs and displays a notification indicating success and
     * the session duration. In case of failure, logs and displays an error notification.
     *
     * @param session  the session start time in milliseconds, used to calculate the session duration.
     * @param linkURL  the URL of the download that is being notified about.
     */
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

    /**
     * Handles cleanup operations when the download task is terminated.
     *
     * This method ensures that resources used during the download process
     * are released properly. It performs the following tasks:
     *
     * 1. Checks if the task is already stopped. If not, it updates the `isStopped` flag to
     *    true and forcibly shuts down all running tasks in the `executorService` by invoking
     *    `shutdownNow()`.
     *
     * 2. Attempts to delete the file located at the `path` if it exists to ensure no
     *    incomplete or corrupted data is left behind.
     *
     * 3. If an I/O exception occurs while deleting the file, it wraps the exception into
     *    a `RuntimeException` and propagates it.
     *
     * This method is invoked during application shutdown or as part of the download task's
     * termination logic to ensure proper resource management and file cleanup.
     */
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

    /**
     * Represents a task responsible for downloading a specific chunk of a file in a multi-threaded download process.
     * This class is designed to handle the downloading of a specified byte range from a given file URL and save it
     * to a target location. It operates as part of a larger parallel download mechanism.
     *
     * The {@code ChunkDownloadTask} is executed as a {@code Runnable} instance and can handle interruptions,
     * periodic I/O operations, and download pausing or stopping as dictated by the parent {@code ParallelDownloadTask}.
     *
     * Features include:
     * - Byte range request capability using HTTP Range headers.
     * - Efficient use of buffer size depending on the underlying storage type.
     * - Support for pause, resume, and stop operations through synchronized methods.
     * - Atomically tracked progress within a multi-threading environment.
     */
    private class ChunkDownloadTask implements Runnable {

        private final String urlString;
        private final String fileDestination;
        private final int start;
        private final int end;

        /**
         * Initializes a task for downloading a specific chunk of a file from a given URL.
         *
         * @param urlString The URL of the file to be downloaded.
         * @param fileDestination The local file path where the downloaded chunk will be saved.
         * @param start The starting byte position of the chunk to be downloaded.
         * @param end The ending byte position of the chunk to be downloaded.
         */
        public ChunkDownloadTask(String urlString, String fileDestination, int start, int end) {
            this.urlString = urlString;
            this.fileDestination = fileDestination;
            this.start = start;
            this.end = end;
        }

        /**
         * Implements a thread that downloads a specific portion of a file from a remote server.
         *
         * This method performs the task of downloading a defined chunk of a file, specified by the start
         * and end byte positions, using an HTTP connection. It sets up a connection to the file's URL,
         * requests the defined byte range to download, and writes the data to a specified file
         * destination.
         *
         * The method utilizes a buffer size optimized for the type of storage and maintains thread
         * safety during the write operation to ensure proper handling of pause and stop commands. The
         * current download progress is tracked and updated throughout the operation.
         *
         * If interrupted or if any exceptions occur, it handles resource cleanup and ensures the
         * connection is safely closed.
         *
         * Exception Handling:
         * - IOException: Captures errors during connection, reading, or writing operations.
         * - InterruptedException: Manages thread interruptions during paused state.
         *
         * Synchronization:
         * - Provides support for pausing and resuming the download thread via synchronization.
         * - Ensures thread-safe handling of stop commands to terminate the download prematurely.
         */
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
