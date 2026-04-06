package com.secondhand.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Common pagination query parameters.")
public class PaginationQuery {

    @Schema(description = "Page number, starting from 1.", example = "1")
    private Integer page;
    @Schema(description = "Number of items per page.", example = "10")
    private Integer limit;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
