package com.example.persistence_repository.persistence.query.common;

public class Order {
    private String column;
    private boolean isAscending;

    public Order(String column, boolean isAscending) {
        this.column = column;
        this.isAscending = isAscending;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public void setAscending(boolean isAscending) {
        this.isAscending = isAscending;
    }

    public static Order asc(String column) {
        return new Order(column, true);
    }

    public static Order desc(String column) {
        return new Order(column, false);
    }

}
