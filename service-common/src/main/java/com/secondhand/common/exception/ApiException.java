package com.secondhand.common.exception;

import lombok.Setter;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    @Setter
    private int code;
    private String code_msg;
    public int getCode() {   // <-- add this
        return code;
    }

    public ApiException(int code) {
        this.code = code;
    }

    public ApiException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ApiException(String message, String code_msg) {
        super(message);
        this.code_msg = code_msg;
    }

    public ApiException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public ApiException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public ApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
                        int code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
