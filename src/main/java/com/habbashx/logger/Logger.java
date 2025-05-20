package com.habbashx.logger;



import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.habbashx.logger.ColoredText.*;

/**
 * The Logger class provides a simple mechanism to log messages at different levels of severity.
 * Each log message is output to the console with a timestamp and formatted with color coding
 * based on the severity level.
 *
 * Key features:
 * - Supports three levels of logging: INFO, WARNING, and ERROR.
 * - Automatically adds a timestamp to each log message.
 * - Outputs log messages with color-coded formatting for better readability.
 *
 * This class is commonly used for tracking and reporting messages within applications.
 */
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

