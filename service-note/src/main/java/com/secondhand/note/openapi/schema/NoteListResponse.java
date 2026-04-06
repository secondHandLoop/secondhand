package com.secondhand.note.openapi.schema;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "NoteListResponse", description = "Successful response envelope for the note list endpoint.")
public class NoteListResponse {

    @Schema(description = "Business status code.", example = "200")
    private Integer status;

    @Schema(description = "Result message.", example = "success")
    private String message;

    @ArraySchema(schema = @Schema(implementation = NoteListItemResponse.class, description = "List of notes visible to the current user."))
    private List<NoteListItemResponse> body;

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

    public List<NoteListItemResponse> getBody() {
        return body;
    }

    public void setBody(List<NoteListItemResponse> body) {
        this.body = body;
    }
}
