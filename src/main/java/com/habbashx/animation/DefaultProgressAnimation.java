package com.habbashx.animation;

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
