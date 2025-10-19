package com.secondhand.common.validate.exception;

public class ValidationException extends RuntimeException {

    private Integer code;

    public ValidationException() {
    }

    public ValidationException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public ValidationException(String message, Throwable cause, Integer code) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}

