package com.disqueprogrammer.app.trackerfinance.dto;

public class ApiSuccessResponse<T> {
    private String status;
    private T data;
    private String message;
}
