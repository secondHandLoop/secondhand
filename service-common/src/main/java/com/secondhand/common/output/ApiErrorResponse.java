package com.secondhand.common.output;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard error response body shared by every service.
 */
@Schema(name = "ApiErrorResponse", description = "Standard error response returned by the services.")
public class ApiErrorResponse {

    @Schema(description = "Business or HTTP-like status code.", example = "400")
    private Integer status;

    @Schema(description = "Human-readable error message.", example = "邮箱不能为空")
    private String message;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
