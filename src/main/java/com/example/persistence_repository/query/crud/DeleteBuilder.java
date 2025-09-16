package com.example.persistence_repository.query.crud;

import java.util.List;

import com.example.persistence_repository.query.AbstractQueryBuilder;

public class DeleteBuilder extends AbstractQueryBuilder {

    public DeleteBuilder(String tableName) {
        super(tableName);
    }

    private String whereClause;

    public static DeleteBuilder builder(String tableName) {
        return new DeleteBuilder(tableName);
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

        StringBuilder query = new StringBuilder("DELETE FROM ").append(tableName);
        if (whereClause != null) {
            query.append(" WHERE " + whereClause);
        }
        return query.toString();

    }

}
