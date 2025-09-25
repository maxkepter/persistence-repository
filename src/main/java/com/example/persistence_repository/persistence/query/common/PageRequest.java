package com.example.persistence_repository.persistence.query.common;

public class PageRequest {
    // 1-indexed
    private int pageNumber;
    private int pageSize;
    private Sort sort;

    public Sort getSort() {
        return sort;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public PageRequest(int pageNumber, int size, Sort sort) {
        this.pageNumber = pageNumber;
        this.pageSize = size;
        this.sort = sort;
    }

    public PageRequest(int pageNumber, int size) {
        this.pageNumber = pageNumber;
        this.pageSize = size;
    }

    public static PageRequest of(int pageNumber, int size, Sort sort) {
        return new PageRequest(pageNumber, size, sort);
    }

    public static PageRequest of(int pageNumber, int size) {
        return new PageRequest(pageNumber, size);
    }

}
