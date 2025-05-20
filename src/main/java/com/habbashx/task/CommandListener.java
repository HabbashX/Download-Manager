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


    }
}
