package com.habbashx.manager;

import com.habbashx.exception.NoSuchAnimationException;
import com.habbashx.exception.NoSuchDownloadMethodException;

public class DownloadTaskFactory {

    public static DownloadTask getInstance(String downloadMethod) throws NoSuchAnimationException, NoSuchDownloadMethodException {

       return switch (downloadMethod) {
            case "single" -> new SingleDownloadTask();
            case "parallel" -> new ParallelDownloadTask();
           default -> throw new NoSuchDownloadMethodException("no such download method: " + downloadMethod);
       };
    }
}
