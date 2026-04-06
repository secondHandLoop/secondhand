package com.secondhand.message.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "MessageApiResponse", description = "Standard success response envelope used by the message service.")
public class ApiResponse<T> {
    @Schema(description = "Business status code.", example = "200")
    private Integer status;
    @Schema(description = "Result message.", example = "success")
    private String message;
    @Schema(description = "Endpoint-specific response body.")
    private T body;
    @Schema(description = "Optional pagination metadata for list endpoints.")
    private Object page;

    public static <T> ApiResponse<T> success(T body) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(200);
        response.setMessage("success");
        response.setBody(body);
        return response;
    }

    public static ApiResponse<Void> success() {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setStatus(200);
        response.setMessage("success");
        return response;
    }

    public static <T> ApiResponse<T> success(T body, Object page) {
        ApiResponse<T> response = success(body);
        response.setPage(page);
        return response;
    }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getBody() { return body; }
    public void setBody(T body) { this.body = body; }
    public Object getPage() { return page; }
    public void setPage(Object page) { this.page = page; }
}
