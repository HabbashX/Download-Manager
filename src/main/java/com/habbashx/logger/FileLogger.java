package com.habbashx.logger;

import lombok.Cleanup;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.apache.commons.csv.CSVFormat.DEFAULT;

/**
 * Handles logging operations to a CSV file for maintaining records of application events such as successes
 * and failures. This logger writes logs in a structured format with pre-defined headers and includes
 * support for retrieving all log entries as an in-memory table.
 *
 * The log file is automatically managed within the class, and new log entries are appended to
 * the existing file. Timestamps are recorded in a consistent format to aid in tracking events.
 *
 * The class includes methods for:
 * - Logging success events.
 * - Logging failure events.
 * - Retrieving all logged entries as an ArrayList of string arrays.
 *
 * Internally, the class crafts log messages with associated metadata like the URL, timestamp,
 * log level, and message content.
 */
public class FileLogger {

    private static final String[] HEADERS = {"URL","Log Date","Log Level","Log Message"};

    private static final String LOG_FILE_NAME = "log.csv";

    private final File file;

    public FileLogger() {
        file = new File(LOG_FILE_NAME);
    }

    @SuppressWarnings("deprecation")
    private void log(String message , String fileURL ,Level level) {

        try {
            boolean fileExists = file.exists();

            @Cleanup
            FileWriter fileWriter =new FileWriter(file,true);

            @Cleanup
            CSVPrinter printer = new CSVPrinter(fileWriter, DEFAULT.withHeader(HEADERS)
                    .withSkipHeaderRecord(fileExists));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            printer.printRecord(fileURL,timestamp,level.toString(),message);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public ArrayList<String[]> getAllLogs(){

        ArrayList<String[]> table = new ArrayList<>();
        try {
            @Cleanup
            Reader reader = new FileReader(file);
            Iterable<CSVRecord> records = DEFAULT.withHeader(HEADERS).withSkipHeaderRecord(true).parse(reader);

            int i = 0;
            for (CSVRecord record : records) {
                if (i == 0) {
                    table.add(HEADERS);
                    i++;
                }
                table.add(new String[]{record.get("URL"),record.get("Log Date"), record.get("Log Level"),record.get("Log Message")});

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return table;
    }

    public void logSuccess(String message, String fileURL) {
        log(message,fileURL,Level.SUCCESS);
    }

    public void logFailure(String message, String fileURL) {
        log(message,fileURL,Level.FAILED);
    }

    private enum Level {
        SUCCESS,
        FAILED
    }

}
