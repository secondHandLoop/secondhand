package com.secondhand.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pagination metadata returned by list endpoints.")
public class PageInfoResponse {
    @Schema(description = "Current page number.", example = "1")
    private Integer page;
    @Schema(description = "Requested page size.", example = "20")
    private Integer limit;
    @Schema(description = "Total number of available records.", example = "56")
    private Long total;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
}
