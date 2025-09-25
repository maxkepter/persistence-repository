package com.example.persistence_repository.persistence.query.crud;

import java.util.List;

import com.example.persistence_repository.persistence.config.BuildQueryConfig;
import com.example.persistence_repository.persistence.query.AbstractQueryBuilder;

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
    public String createQuery() {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("Table name is required for DELETE statement.");
        }
        StringBuilder query = new StringBuilder("DELETE FROM ").append(tableName);
        if (whereClause != null && !whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        return query.toString();
    }

    @Override
    public String build(boolean isPrintSql) {
        String query = createQuery();
        if (isPrintSql) {
            System.out.println("Generated Query: " + query);
        }
        return query;
    }

    @Override
    public String build() {
        String query = createQuery();
        if (BuildQueryConfig.isPrintSql) {
            System.out.println("Generated Query: " + query);
        }
        return query;
    }

}
