package com.example.persistence_repository.query.crud;

import java.util.ArrayList;
import java.util.List;

import com.example.persistence_repository.query.AbstractQueryBuilder;
import com.example.persistence_repository.query.Order;

public class SelectBuilder extends AbstractQueryBuilder {

    private List<String> columns;
    private boolean isDistinct;
    private String whereClause;
    List<Order> orderByColumns;
    private Integer limit;
    private Integer offset;

    public SelectBuilder(String tableName) {
        super(tableName);
        this.orderByColumns = new ArrayList<>();
    }

    public static SelectBuilder builder(String tableName) {
        return new SelectBuilder(tableName);
    }

    public SelectBuilder columns(String... columns) {
        this.columns = List.of(columns);
        return this;
    }

    public SelectBuilder columns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public SelectBuilder distinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
        return this;
    }

    public SelectBuilder where(String whereClause, Object... params) {
        this.getParameters().addAll(List.of(params));
        this.whereClause = whereClause;
        return this;
    }

    public SelectBuilder orderBy(List<Order> orderByColumns) {
        this.orderByColumns = orderByColumns;
        return this;
    }

    public SelectBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public SelectBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public String build() {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("Table name is required for SELECT query");
        }
        StringBuilder query = new StringBuilder("SELECT ");
        if (isDistinct) {
            query.append("DISTINCT ");
        }
        if (columns == null || columns.isEmpty()) {
            query.append("*");
        } else {
            query.append(String.join(", ", columns));
        }
        query.append(" FROM ").append(tableName);
        if (whereClause != null && !whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        if (orderByColumns != null && !orderByColumns.isEmpty()) {
            query.append(" ORDER BY ");
            for (int i = 0; i < orderByColumns.size(); i++) {
                Order order = orderByColumns.get(i);
                query.append(order.getColumn()).append(order.isAscending() ? " ASC" : " DESC");
                if (i < orderByColumns.size() - 1) {
                    query.append(", ");
                }
            }
        }
        if (limit != null) {
            query.append(" LIMIT ").append(limit);
        }
        if (offset != null) {
            query.append(" OFFSET ").append(offset);
        }
        return query.toString();
    }

}
