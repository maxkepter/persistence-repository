package com.example.persistence_reposistory.query;

import java.util.ArrayList;
import java.util.List;

public class InsertBuilder extends AbstractQueryBuilder {
    private String tableName;
    private List<String> columns;
    private List<Object> values;

    public InsertBuilder() {
        this.values = new ArrayList<>();
    }

    public static InsertBuilder builder() {
        return new InsertBuilder();
    }

    public InsertBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public InsertBuilder columns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public InsertBuilder columns(String... columns) {
        this.columns = List.of(columns);
        return this;
    }

    public InsertBuilder values(List<Object> values) {
        this.values = values;
        return this;
    }

    public InsertBuilder values(Object... values) {
        this.values.addAll(List.of(values));
        return this;
    }

    @Override
    public String build() {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("Table name is required for INSERT query");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalStateException("At least one column is required for INSERT query");
        }
        if (values == null || values.isEmpty() || values.size() != columns.size()) {
            throw new IllegalStateException("Values must be provided for all columns in INSERT query");
        }

        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(tableName).append(" (");
        query.append(String.join(", ", columns));
        query.append(") VALUES (");
        query.append(String.join(", ", values.stream().map(v -> "?").toList()));
        query.append(")");

        this.setParameters(values);
        return query.toString();
    }

}
