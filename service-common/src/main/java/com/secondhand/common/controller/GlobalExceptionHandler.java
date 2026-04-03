package com.secondhand.common.controller;

import com.secondhand.common.exception.ApiException;
import com.secondhand.common.output.ApiResponseHelper;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ApiResponseHelper {

    @ExceptionHandler(ApiException.class)
    public Map<String, Object> handleApiException(ApiException ex) {
        int code = ex.getCode() == 0 ? 500 : ex.getCode();
        return fail(ex.getMessage(), code);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class
    })
    public Map<String, Object> handleBadRequest(Exception ex) {
        return fail(ex.getMessage(), 400);
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception ex) {
        return fail(ex.getMessage(), 500);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Map<String, Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return fail("Uploaded files are too large. Please keep each upload under 10MB and the total request under 30MB.", 413);
    }
}
