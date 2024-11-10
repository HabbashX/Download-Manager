package com.habbashx;

import com.habbashx.config.DownloadManagerConfig;

import com.habbashx.exception.NoSuchAnimationException;
import com.habbashx.exception.NoSuchDownloadMethodException;

import com.habbashx.manager.DownloadTask;
import com.habbashx.manager.DownloadTaskFactory;

import com.habbashx.task.CommandListener;

import com.habbashx.terminal.ResizableTable;

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
                new Thread(new CommandListener(downloadTask , linkURL)).start();
                downloadTask.downloadFile(linkURL);
            }
            default -> System.out.println("use --help");
        }

    }
}