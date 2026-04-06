package com.secondhand.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after uploading a profile photo.")
public class UploadPhotoResponse {
    @Schema(description = "Public URL of the uploaded photo.", example = "https://cdn.example.com/avatar.jpg")
    private String url;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
