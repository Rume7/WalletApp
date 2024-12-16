package com.swisspost.exception;

public class InvalidAssetQuantityException extends RuntimeException {

    public InvalidAssetQuantityException(String message) {
        super(message);
    }
}
