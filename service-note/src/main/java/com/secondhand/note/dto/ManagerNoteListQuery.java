package com.secondhand.note.dto;

import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Query parameters accepted by the manager note list endpoint.")
public class ManagerNoteListQuery {

    @Schema(description = "Page number, starting from 1.", example = "1")
    private Integer page;
    @Schema(description = "Number of notes per page.", example = "20")
    private Integer limit;
    @Schema(description = "Keyword matched against owner, title, description, city, and state.", example = "desk")
    private String keyword;
    @Schema(description = "Filter by owner user id.", example = "88")
    private Long userId;
    @Schema(description = "Filter by note publication status.")
    private NoteStatus noteStatus;
    @Schema(description = "Filter by top-level category.")
    private TopCategory topCategory;
    @Schema(description = "Filter by whether delivery is available.", example = "true")
    private Boolean delivery;
    @Schema(description = "Filter by record status such as ACTIVE or DEL.", example = "ACTIVE")
    private String status;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public NoteStatus getNoteStatus() { return noteStatus; }
    public void setNoteStatus(NoteStatus noteStatus) { this.noteStatus = noteStatus; }
    public TopCategory getTopCategory() { return topCategory; }
    public void setTopCategory(TopCategory topCategory) { this.topCategory = topCategory; }
    public Boolean getDelivery() { return delivery; }
    public void setDelivery(Boolean delivery) { this.delivery = delivery; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
