package com.secondhand.message.dto;

public class PaginationQuery {
    private Integer page;
    private Integer limit;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
}
