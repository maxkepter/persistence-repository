package com.example.persistence_repository.persistence.query.crud;

import java.util.ArrayList;
import java.util.List;
import com.example.persistence_repository.persistence.config.BuildQueryConfig;
import com.example.persistence_repository.persistence.query.AbstractQueryBuilder;

public class UpdateBuilder extends AbstractQueryBuilder {
    private List<String> setClauses;
    private String whereClause;

    public UpdateBuilder(String tableName) {
        super(tableName);
        setClauses = new ArrayList<>();
    }

    public static UpdateBuilder builder(String tableName) {
        return new UpdateBuilder(tableName);
    }

    public UpdateBuilder set(String column, Object value) {
        this.getParameters().add(value);
        this.setClauses.add(column);
        return this;
    }

    public UpdateBuilder where(String whereClause, Object... params) {
        this.getParameters().addAll(List.of(params));
        this.whereClause = whereClause;
        return this;
    }

    @Override
    public String createQuery() {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("Table name is required for UPDATE statement.");
        }
        if (setClauses.isEmpty()) {
            throw new IllegalStateException("At least one SET clause is required for UPDATE statement.");
        }

        StringBuilder query = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        query.append(setClauses.stream().map(col -> col + " = ?")
                .reduce((a, b) -> a + "," + b).orElse(""));

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
