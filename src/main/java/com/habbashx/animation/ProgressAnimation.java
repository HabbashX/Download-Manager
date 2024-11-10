package com.habbashx.animation;

public abstract class ProgressAnimation {

    protected static final int MEGA_BYTES = 1024;
    protected static final int GIGA_BYTES = 1048576;

    public abstract void printProgressBar(int progress, int barLength, double internetSpeed, long downloadedBytes ,int fileSize);

}
