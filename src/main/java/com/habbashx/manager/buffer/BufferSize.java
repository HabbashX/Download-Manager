package com.habbashx.manager.buffer;

import com.habbashx.system.StorageType;

/**
 * The BufferSize class provides a recommendation for buffer size based on the type of storage.
 * It determines an optimal buffer size to enhance performance during file-related operations such as file transfers or downloads.
 */
public class BufferSize {

    /**
     * Represents the type of storage being used. This variable holds a constant reference to a specific storage type.
     * It provides an immutable representation of the storage type configuration for use throughout the application.
     */
    private final StorageType storageType;

    public BufferSize(StorageType storageType) {
        this.storageType = storageType;
    }

    /**
     * Determines the recommended buffer size based on the type of storage being used.
     * The buffer size may vary depending on whether the storage type is SSD or not.
     *
     * @return the recommended buffer size in bytes. Returns 122880 if the storage type is SSD,
     *         otherwise returns 65536.
     */
    public int getRecommendedBufferSize(){
        if (storageType.equals(StorageType.SSD)){
            return 122880;
        }else {
            return 65536;
        }
    }
}
