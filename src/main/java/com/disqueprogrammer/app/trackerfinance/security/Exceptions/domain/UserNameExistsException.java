package com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain;

public class UserNameExistsException extends  Exception {
    public UserNameExistsException(String message) {
        super(message);
    }
}
