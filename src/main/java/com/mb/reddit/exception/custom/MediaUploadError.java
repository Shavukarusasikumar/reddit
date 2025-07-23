package com.mb.reddit.exception.custom;

public class MediaUploadError extends RuntimeException{

    public MediaUploadError(String message) {
        super(message);
    }
}
