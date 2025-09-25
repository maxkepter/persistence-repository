package com.example.persistence_repository.persistence.query.common;

public class Page<T> {
    private long totalElements;
    private PageRequest pageRequest;
    private Iterable<T> content;

    public Page(long totalElements, PageRequest pageRequest, Iterable<T> content) {
        this.totalElements = totalElements;
        this.pageRequest = pageRequest;
        this.content = content;
    }

    public int getTotalPages() {
        if (pageRequest.getPageSize() == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageRequest.getPageSize());
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }

    public Iterable<T> getContent() {
        return content;
    }

    public void setContent(Iterable<T> content) {
        this.content = content;
    }

    public boolean isFirst() {
        return pageRequest.getPageNumber() == 0;
    }

    public boolean isLast() {
        return pageRequest.getPageNumber() >= getTotalPages() - 1;
    }

    public boolean hasNext() {
        return pageRequest.getPageNumber() < getTotalPages() - 1;
    }
}
