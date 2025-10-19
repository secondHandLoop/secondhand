package com.secondhand.common.exception;


public class PassException extends RuntimeException {

    public PassException() {
    }

    public PassException(String message) {
        super(message);
    }

    public PassException(String message, Throwable cause) {
        super(message, cause);
    }

    public PassException(Throwable cause) {
        super(cause);
    }

    public PassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

