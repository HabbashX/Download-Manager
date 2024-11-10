package com.habbashx.logger;



import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.habbashx.logger.ColoredText.*;

public class Logger {

    private void log(Level level, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

        String color = switch (level) {
            case INFO -> LIME_GREEN;
            case WARNING -> BRIGHT_RED;
            case ERROR -> RED;
        };
        System.out.printf("%s[%s%s%s] %s[%s%s%s] %s%s \n",
                GRAY, BRIGHT_YELLOW,timestamp, GRAY,
                GRAY,color,level, GRAY, RESET,message
        );
    }

    public void info(String message) {
        log(Level.INFO, message);
    }
    public void warning(String message) {
        log(Level.WARNING, message);
    }
    public void error(String message) {
        log(Level.ERROR, message);
    }

    enum Level {

        INFO,
        ERROR,
        WARNING
    }
}

