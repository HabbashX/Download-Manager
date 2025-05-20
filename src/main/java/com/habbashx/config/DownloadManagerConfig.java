package com.habbashx.config;

import lombok.Cleanup;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;


import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Properties;

/**
 * This class represents the configuration management utility for the DownloadManager application.
 * It allows reading, writing, and maintaining configuration properties using a dedicated configuration file.
 * The configurations are stored in a properties file named "downloadManager.properties."
 *
 * Responsibilities include:
 * - Loading existing configurations from the file.
 * - Creating and initializing a default configuration file if it does not exist.
 * - Modifying existing property values and persisting them to the file.
 * - Providing type-safe parsing of property values.
 *
 * Methods:
 * - The constructor initializes the configuration by checking for the file's existence, creating it if necessary,
 *   and loading the existing properties upon application start.
 * - `loadConfiguration`: Reads the properties from the configuration file and loads them into a `Properties` object.
 * - `getPropertyValue`: Retrieves the value of a specific property and parses it into its appropriate data type
 *   (e.g., String, Integer, Double, or Boolean) using `PropertyParser`.
 * - `modifyProperty`: Updates the value of a specific property and persists the changes back to the configuration file.
 * - `generateConfigurationFile`: Ensures the configuration file is created if it does not already exist
 *   and initializes it with default settings.
 */
public class DownloadManagerConfig {

    private final Properties properties;

    /**
     * Represents the name of the configuration file used by the application
     * to store and load properties related to the download manager. This
     * file contains settings such as timeout, speed limits, and animation preferences.
     *
     * The file is named {@code downloadManager.properties} by default and is
     * created automatically if it does not already exist. It serves as the
     * persistent storage for all configurable parameters of the download manager.
     */
    private static final String CONFIGURATION_FILE = "downloadManager.properties";

    public DownloadManagerConfig() {
        this.properties = new Properties();
        generateConfigurationFile();
        loadConfiguration(); // load all configuration
    }

    /**
     * Loads the application's configuration properties from an external file.
     *
     * This method initializes the `properties` object by reading key-value
     * pairs from the configuration file specified by the `CONFIGURATION_FILE`
     * field of the containing class. Using an {@link InputStream}, it reads
     * the configuration in a try-with-resources block to ensure that the stream
     * is closed automatically after use.
     *
     * If the configuration file cannot be accessed or read due to an I/O
     * issue, the method throws a {@link RuntimeException} wrapping the
     * underlying {@link IOException}.
     *
     * This method is typically invoked during the initialization process
     * to ensure that the application loads existing configurations.
     *
     * Throws:
     * - RuntimeException: If there is an I/O error while reading the configuration file.
     */
    public void loadConfiguration(){

        try (InputStream inputStream = new FileInputStream(CONFIGURATION_FILE)) {
            properties.load(inputStream);
        }catch (IOException e){
           throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the value of a specified property from the application's configuration.
     * The method attempts to parse the property value into its appropriate data type
     * (e.g., boolean, integer, double, or string) using a utility parser.
     *
     * @param property the key of the property to retrieve
     * @return the parsed value of the specified property; can be a Boolean, Integer,
     *         Double, or String depending on the property's value
     */
    public Object getPropertyValue(String property){
        String value = properties.getProperty(property);
        return PropertyParser.parsePropertyValue(value);
    }

    /**
     * Modifies the value of a specified property and updates the configuration file
     * with the new value. The method ensures that the updated property is persisted
     * in the external configuration file.
     *
     * @param property  the name of the property to be updated
     * @param newValue  the new value to assign to the specified property
     */
    public void modifyProperty(String property , String newValue){
        properties.setProperty(property, newValue);
        try {
            properties.store(new FileOutputStream(CONFIGURATION_FILE), """
                    # all rights reserved to HabbashX
                    # NOTE: choose parallel download method for better performance
                    #
                    # animations [default , arrow , rainbow]
                    # download methods [single , parallel]
                    """);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a default configuration file if it does not already exist.
     * The generated configuration file contains predefined settings and comments
     * that guide users to understand and modify the available configuration options.
     *
     * The method ensures the file is created and writes the following settings:
     * - Timeout duration for download operations.
     * - Speed limit for downloads.
     * - The type of progress animation.
     * - The chosen download method (e.g., single or parallel).
     *
     * In case of an error during file creation or writing, an unchecked
     * {@link RuntimeException} is thrown wrapping the underlying {@link IOException}.
     *
     * This method is private as it is intended to be used internally by the class
     * to initialize the configuration file during the setup process.
     */
    private void generateConfigurationFile(){

        try {
            Path path = Path.of(CONFIGURATION_FILE);
           if (!Files.exists(path)) {
               Files.createFile(path);
               @Cleanup
               FileWriter fileWriter = new FileWriter(CONFIGURATION_FILE);
               fileWriter.write("""
                    # all rights reserved to HabbashX
                    # NOTE: choose parallel download method for better performance
                    #
                    # animations [default , arrow , rainbow]
                    # download methods [single , parallel]
                    
                    dm.settings.timeout = 400000
                    dm.settings.speedLimit = 100
                    dm.settings.progress.animation = default
                    dm.settings.download.method = parallel
                    """);
           }
        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }
}
