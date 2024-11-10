package com.habbashx.manager.buffer;

import com.habbashx.system.StorageType;

public class BufferSize {

    private final StorageType storageType;

    public BufferSize(StorageType storageType) {
        this.storageType = storageType;
    }

    public int getRecommendedBufferSize(){
        if (storageType.equals(StorageType.SSD)){
            return 122880;
        }else {
            return 65536;
        }
    }
}
