package com.habbashx.animation;

/**
 * Provides a concrete implementation of a progress animation, displaying a progress bar
 * with detailed information about the progress percentage, current internet download speed,
 * and downloaded data relative to the total file size. The progress bar uses '#' to represent
 * the completed portion and '-' to represent the remaining portion, dynamically updating in real time.
 *
 * This class is an implementation of the abstract {@code ProgressAnimation} class, and it overrides
 * the {@code printProgressBar} method to provide the functionality for visualizing progress as
 * described above. The progress bar is displayed in the following format:
 *
 * - A dynamic progress bar visualization that adjusts based on the progress percentage and total bar length.
 * - Display of the internet speed in appropriate units (KB/s, MB/s, or GB/s) based on the current speed value.
 * - Information on downloaded megabytes relative to the total file size.
 */
class DefaultProgressAnimation extends ProgressAnimation {

    @Override
    public void printProgressBar(int progress , int barLength , double internetSpeed, long downloadedBytes, int fileSize){

        int filledLength = (int) (barLength * progress / 100.0);

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                builder.append("#");
            } else {
                builder.append("-");
            }
        }

        long downloadedMegaBytes = (downloadedBytes / 1024) / 1024;
        long fileSizeMega = (fileSize / 1024) / 1024;

        float speed;
        String format;

        if (internetSpeed >= GIGA_BYTES){
            speed = (float) ((internetSpeed /1024) /1024);
            format = "GB/s";
        } else if (internetSpeed >= MEGA_BYTES) {
            speed = (float) (internetSpeed / 1024);
            format = "MB/s";
        } else {
            speed = (int) internetSpeed;
            format = "KB/s";
        }
        System.out.print("\rProgress: "+"["+builder+"]"+ progress+"%"+" internet Speed: %s".formatted(speed) +" %s".formatted(format)
                +" Downloaded "+downloadedMegaBytes+"/"+fileSizeMega +"MB"
        );
    }
}
