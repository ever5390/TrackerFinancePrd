package com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain;

public class EmailExistsException extends Exception {
    public EmailExistsException(String message) {
        super(message);
    }
}
