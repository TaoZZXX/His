package com.his.domain;

import java.util.List;

public class PageResult<T> {
    private List<T> list;

    private Long total;

    private Integer pageSize;

    private Integer pageNo;

    private Integer pages; // 总页数

    public PageResult() {
    }

    public PageResult(List<T> list, Long total, Integer pageSize, Integer pageNo, Integer pages) {
        this.list = list;
        this.total = total;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }
}
