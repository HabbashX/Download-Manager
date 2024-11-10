package com.habbashx.exception;

public class UnSupportedSystemTray extends RuntimeException {
    public UnSupportedSystemTray(String message) {
        super(message);
    }
}
