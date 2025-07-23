package com.mb.reddit.exception.custom;

public class CommunityNotFoundException extends RuntimeException {

    public CommunityNotFoundException(String message) {
        super(message);
    }
}
