package com.example.persistence_repository.persistence.query.common;

import java.util.List;

public class Sort {
    private List<Order> orders;

    public Sort(List<Order> orders) {
        this.orders = orders;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public static Sort unsorted() {
        return new Sort(List.of());
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public static Sort by(Order... orders) {
        Sort sort = new Sort(List.of(orders));
        return sort;
    }
}
