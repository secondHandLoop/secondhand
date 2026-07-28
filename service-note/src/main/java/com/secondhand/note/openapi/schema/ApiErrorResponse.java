package com.secondhand.note.openapi.schema;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiErrorResponse", description = "Standard error response envelope.")
public class ApiErrorResponse {

    @Schema(description = "Business or HTTP-like status code returned by the service.", example = "400")
    private Integer status;

    @Schema(description = "Human-readable error message.", example = "Failed to convert value of type.")
    private String message;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
