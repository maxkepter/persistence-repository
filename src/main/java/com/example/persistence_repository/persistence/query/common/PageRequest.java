package com.example.persistence_repository.persistence.query.common;

/**
 * Represents a request for a specific page of data with optional sorting.
 * <p>
 * This class is used to encapsulate pagination information such as the page
 * number (1-indexed),
 * page size, and sorting criteria. It provides factory methods for convenient
 * instantiation.
 * </p>
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * PageRequest request = PageRequest.of(1, 20, Sort.by("name"));
 * </pre>
 * </p>
 *
 * 
 * 
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 */
public class PageRequest {
    // 1-indexed page number
    private int pageNumber;
    // Number of items per page
    private int pageSize;
    // Sorting criteria
    private Sort sort;

    /**
     * Returns the sorting criteria.
     */
    public Sort getSort() {
        return sort;
    }

    /**
     * Returns the page size.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Returns the page number (1-indexed).
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Constructs a PageRequest with page number, size, and sorting.
     * 
     * @param pageNumber the page number
     * @param size       the page size
     * @param sort       the sorting criteria
     */
    public PageRequest(int pageNumber, int size, Sort sort) {
        this.pageNumber = pageNumber;
        this.pageSize = size;
        this.sort = sort;
    }

    /**
     * Constructs a PageRequest with page number and size, without sorting.
     * 
     * @param pageNumber the page number
     * @param size       the page size
     */
    public PageRequest(int pageNumber, int size) {
        this.pageNumber = pageNumber;
        this.pageSize = size;
    }

    /**
     * Factory method to create a PageRequest with sorting.
     * 
     * @param pageNumber the page number (1-indexed)
     * @param size       the page size (must be greater than 0)
     * @param sort       the sorting criteria (must not be null)
     * @throws IllegalArgumentException if pageNumber < 1, size < 1, or sort is null
     * @return a new PageRequest instance
     */
    public static PageRequest of(int pageNumber, int size, Sort sort) {
        validate(pageNumber, size);
        if (sort == null) {
            throw new IllegalArgumentException("sort must not be null");
        }
        return new PageRequest(pageNumber, size, sort);
    }

    /**
     * Factory method to create a PageRequest without sorting.
     * 
     * @param pageNumber the page number (1-indexed)
     * @param size       the page size (must be greater than 0)
     * @throws IllegalArgumentException if pageNumber < 1 or size < 1
     * @return a new PageRequest instance
     */
    public static PageRequest of(int pageNumber, int size) {
        validate(pageNumber, size);
        return new PageRequest(pageNumber, size);
    }

    private static void validate(int pageNumber, int size) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
    }
}
