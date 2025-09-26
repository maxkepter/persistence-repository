
package com.example.persistence_repository.persistence.query.common;

/**
 * Represents a paginated result set for a query.
 * <p>
 * This class encapsulates the content of a single page, the total number of
 * elements,
 * and the pagination information via {@link PageRequest}.
 *
 * @param <T> the type of elements in the page content
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 */
public class Page<T> {
    // The total number of elements across all pages.
    private long totalElements;

    // The pagination information for this page.
    private PageRequest pageRequest;

    // The content of this page.
    private Iterable<T> content;

    /**
     * Constructs a new Page with the given total elements, page request, and
     * content.
     *
     * @param totalElements the total number of elements
     * @param pageRequest   the pagination information
     * @param content       the content of the current page
     */
    public Page(long totalElements, PageRequest pageRequest, Iterable<T> content) {
        this.totalElements = totalElements;
        this.pageRequest = pageRequest;
        this.content = content;
    }

    /**
     * Returns the total number of pages.
     *
     * @return total pages
     */
    public int getTotalPages() {
        if (pageRequest.getPageSize() == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageRequest.getPageSize());
    }

    /**
     * Returns the total number of elements.
     *
     * @return total elements
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Returns the pagination information for this page.
     *
     * @return page request
     */
    public PageRequest getPageRequest() {
        return pageRequest;
    }

    /**
     * Returns the content of this page.
     *
     * @return page content
     */
    public Iterable<T> getContent() {
        return content;
    }

    /**
     * Returns true if this is the first page.
     *
     * @return true if first page, false otherwise
     */
    public boolean isFirst() {
        return pageRequest.getPageNumber() == 0;
    }

    /**
     * Returns true if this is the last page.
     *
     * @return true if last page, false otherwise
     */
    public boolean isLast() {
        return pageRequest.getPageNumber() >= getTotalPages() - 1;
    }

    /**
     * Returns true if there is a next page.
     *
     * @return true if next page exists, false otherwise
     */
    public boolean hasNext() {
        return pageRequest.getPageNumber() < getTotalPages() - 1;
    }
}
