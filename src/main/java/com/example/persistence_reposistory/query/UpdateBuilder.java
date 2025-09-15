package com.example.persistence_reposistory.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateBuilder extends AbstractQueryBuilder {
    private String tableName;
    private Map<String, Object> setClauses;
    private String whereClause;

    public UpdateBuilder() {
        setClauses = new HashMap<>();
    }

    public static UpdateBuilder builder() {
        return new UpdateBuilder();
    }

    public UpdateBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public UpdateBuilder set(String column, Object value) {
        this.getParameters().add(value);
        this.setClauses.put(column, value);
        return this;
    }

    public UpdateBuilder where(String whereClause, Object... params) {
        this.getParameters().addAll(List.of(params));
        this.whereClause = whereClause;
        return this;
    }

    @Override
    public String build() {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("Table name is required for UPDATE statement.");
        }
        if (setClauses.isEmpty()) {
            throw new IllegalStateException("At least one SET clause is required for UPDATE statement.");
        }

        StringBuilder query = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        query.append(setClauses.entrySet().stream().map(e -> e.getKey() + " = " + e.getValue())
                .reduce((a, b) -> b + "," + a).orElse(""));

        if (whereClause != null) {
            query.append(whereClause);
        }

        return query.toString();
    }

}
