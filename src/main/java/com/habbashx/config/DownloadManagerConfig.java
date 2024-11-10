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

public class DownloadManagerConfig {

    private final Properties properties;

    private static final String CONFIGURATION_FILE = "downloadManager.properties";

    public DownloadManagerConfig() {
        this.properties = new Properties();
        generateConfigurationFile();
        loadConfiguration(); // load all configuration
    }

    public void loadConfiguration(){

        try (InputStream inputStream = new FileInputStream(CONFIGURATION_FILE)) {
            properties.load(inputStream);
        }catch (IOException e){
           throw new RuntimeException(e);
        }
    }

    public Object getPropertyValue(String property){
        String value = properties.getProperty(property);
        return PropertyParser.parsePropertyValue(value);
    }

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
