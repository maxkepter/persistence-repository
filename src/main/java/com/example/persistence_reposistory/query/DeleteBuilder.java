package com.example.persistence_reposistory.query;

import java.util.List;

public class DeleteBuilder extends AbstractQueryBuilder {
    private String tableName;

    private String whereClause;

    public static DeleteBuilder builder() {
        return new DeleteBuilder();
    }

    public DeleteBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public DeleteBuilder where(String whereClause, Object... values) {
        this.whereClause = whereClause;
        this.getParameters().addAll(List.of(values));
        return this;
    }

    @Override
    public String build() {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("Table name is required for DELETE statement.");
        }

        StringBuilder query = new StringBuilder("DELETE FORM ").append(tableName);
        if (whereClause != null) {
            query.append(" WHERE");
        }
        return query.toString();

    }

}
