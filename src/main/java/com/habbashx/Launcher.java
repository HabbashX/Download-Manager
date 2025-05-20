package com.habbashx;

import com.habbashx.config.DownloadManagerConfig;

import com.habbashx.exception.NoSuchAnimationException;
import com.habbashx.exception.NoSuchDownloadMethodException;

import com.habbashx.manager.DownloadTask;
import com.habbashx.manager.DownloadTaskFactory;

import com.habbashx.task.CommandListener;

import com.habbashx.terminal.ResizableTable;

/**
 * Abstract class that provides a method to initialize and manage the execution of
 * different download-related operations based on the provided command-line arguments.
 * <br><br>
 * This class delegates configuration parsing and instantiation of required components
 * (e.g., {@link DownloadTask}) to facilitate downloading files or handling operational commands
 * such as printing logs, modifying configuration, or fetching version information.
 * <br><br>
 * The class contains a static method, {@code launch}, that performs the following tasks:
 * - Parses command-line arguments and dispatches the requested functionality.
 * - Handles operations like displaying the help menu, version information, and logs.
 * - Configures the application using runtime-modifiable settings.
 * - Initializes the appropriate {@link DownloadTask} implementation for downloading files.
 * <br><br>
 * Error handling is implemented to manage custom exceptions such as {@code NoSuchAnimationException}
 * and {@code NoSuchDownloadMethodException}, which may occur due to misconfigurations or unsupported
 * download methods.
 */
public abstract class Launcher {

    public static void launch(String[] args) throws NoSuchAnimationException, NoSuchDownloadMethodException {
        DownloadManagerConfig config = new DownloadManagerConfig();
        String downloadMethod = (String) config.getPropertyValue("dm.settings.download.method");
        DownloadTask downloadTask = DownloadTaskFactory.getInstance(downloadMethod);

        switch (args[0]) {
            case "--help" -> System.out.println("""
                        --version print program version
                        --logs print downloads logs
                        --config <property> <new Value>
                        -d <linkURL>
                        """);
            case "--logs" -> ResizableTable.printTable(downloadTask.getFileLogger().getAllLogs());
            case "--version" -> System.out.println("1.0-alpha");
            case "--config" -> config.modifyProperty(args[1], args[2]);
            case "-d" -> {
                String linkURL = args[1];
                downloadTask.downloadFile(linkURL);
            }
            default -> System.out.println("use --help");
        }

    }
}