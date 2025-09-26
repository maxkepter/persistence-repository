package com.example.persistence_repository.persistence.query.common;

/**
 * Represents an ordering for a query, specifying the column and direction
 * (ascending or descending).
 * 
 * @author Kepter
 * @since 1.0
 * 
 */
public class Order {
    // column name to order by
    private String column;
    // true for ASC, false for DESC
    private boolean isAscending;

    /**
     * Validates that the column name is not null or empty.
     * 
     * @param column the column name to validate
     */
    private void validate(String column) {
        if (column == null || column.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name must not be null or empty.");
        }
    }

    /**
     * Constructs an Order object with the specified column and direction.
     * 
     * @param column      the column name to order by
     * @param isAscending true for ascending, false for descending
     */
    public Order(String column, boolean isAscending) {
        validate(column);
        this.column = column;
        this.isAscending = isAscending;
    }

    /**
     * Gets the column name.
     * 
     * @return the column name
     */
    public String getColumn() {
        return column;
    }

    /**
     * Checks if the order is ascending.
     * 
     * @return true if ascending, false if descending
     */
    public boolean isAscending() {
        return isAscending;
    }

    /**
     * Sets the order direction.
     * 
     * @param isAscending true for ascending, false for descending
     */
    public void setAscending(boolean isAscending) {
        this.isAscending = isAscending;
    }

    /**
     * Creates an ascending Order for the specified column.
     * 
     * @param column the column name
     * @return an Order object with ascending direction
     */
    public static Order asc(String column) {
        return new Order(column, true);
    }

    /**
     * Creates a descending Order for the specified column.
     * 
     * @param column the column name
     * @return an Order object with descending direction
     */
    public static Order desc(String column) {
        return new Order(column, false);
    }

}
