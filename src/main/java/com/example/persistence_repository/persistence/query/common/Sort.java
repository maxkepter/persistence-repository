package com.example.persistence_repository.persistence.query.common;

import java.util.List;

/**
 * Represents sorting criteria for queries.
 * Contains a list of {@link Order} objects specifying the sort order.
 * <p>
 * Example usage:
 * 
 * <pre>
 * Sort sort = Sort.by(Order.desc("id"), Order.asc("age"));
 * </pre>
 * </p>
 * 
 * @params orders can not be null or empty no need to check, want to know why?
 *         Read source code of by() method
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 */
public class Sort {
    // List of Order objects representing sorting criteria
    private List<Order> orders;

    /**
     * Private constructor to create a Sort instance with the given orders.
     * 
     * @param orders List of Order objects
     */
    private Sort(List<Order> orders) {
        this.orders = orders;
    }

    /**
     * Returns the list of Order objects representing the sorting criteria.
     * 
     * @return List of Order objects
     */
    public List<Order> getOrders() {
        return orders;
    }

    /**
     * Returns a Sort instance with no sorting criteria (unsorted).
     * 
     * @return Sort instance with empty orders list
     */
    public static Sort unsorted() {
        // Return a Sort instance with an empty list, representing unsorted
        return new Sort(List.of(new Order[0]));
    }

    /**
     * Creates a Sort instance with the specified Order objects.
     * 
     * @param orders Varargs of Order objects
     * @return Sort instance with the given orders
     * @throws IllegalArgumentException if orders is null or empty
     */
    public static Sort by(Order... orders) {
        if (orders == null || orders.length == 0) {
            throw new IllegalArgumentException("orders must not be null or empty");
        }
        return new Sort(List.of(orders));
    }
}
