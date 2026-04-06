package com.secondhand.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload containing a target note id.")
public class NoteIdRequest {

    @Schema(description = "Target note id.", example = "123")
    private Long noteId;

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }
}
