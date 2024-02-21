package com.disqueprogrammer.app.trackerfinance.exception.domain;

public class NewBalanceLessThanCurrentBalanceException extends Exception {
    public NewBalanceLessThanCurrentBalanceException(String message) {
        super(message);
    }
}
