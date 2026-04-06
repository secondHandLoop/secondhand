package com.secondhand.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after uploading one note media file.")
public class UploadMediaResponse {

    @Schema(description = "Public URL of the uploaded media.", example = "https://cdn.example.com/notes/123/photo-1.jpg")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
