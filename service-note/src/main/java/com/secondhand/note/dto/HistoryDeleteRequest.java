package com.secondhand.note.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request payload used to delete one or more history entries.")
public class HistoryDeleteRequest {

    @ArraySchema(schema = @Schema(description = "Note ids whose history records should be deleted.", example = "123"))
    private List<Long> noteIds;

    public List<Long> getNoteIds() {
        return noteIds;
    }

    public void setNoteIds(List<Long> noteIds) {
        this.noteIds = noteIds;
    }
}
