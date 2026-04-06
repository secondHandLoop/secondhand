package com.secondhand.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserApiErrorResponse", description = "Standard error response returned by the user service.")
public class ApiErrorResponse {

    @Schema(description = "Business or HTTP-like status code.", example = "400")
    private Integer status;

    @Schema(description = "Human-readable error message.", example = "邮箱不能为空")
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
