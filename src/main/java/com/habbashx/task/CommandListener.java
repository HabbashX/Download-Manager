package com.habbashx.task;

import com.habbashx.manager.DownloadTask;

import java.util.Scanner;

public class CommandListener implements Runnable {

    private final DownloadTask task;
    private final String linkURL;

    public CommandListener(DownloadTask task , String linkURL) {
        this.task = task;
        this.linkURL = linkURL;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

            System.out.println("enter command [PAUSE , RESUME , STOP] the download");
            while (true) {
                String command = scanner.next().toLowerCase();
               if (command.equalsIgnoreCase("pause")) {
                   task.pauseDownload();
               } else if (command.equalsIgnoreCase("resume")) {
                   task.resumeDownload(linkURL);
               } else if (command.equalsIgnoreCase("stop")) {
                   task.stopDownload();
                   break;
               }
            }
    }
}
