package com.example.persistence_repository.persistence.query.crud;

import java.util.ArrayList;
import java.util.List;

import com.example.persistence_repository.persistence.config.RepositoryConfig;
import com.example.persistence_repository.persistence.query.AbstractQueryBuilder;

/**
 * A builder class for constructing SQL INSERT queries.
 * <p>
 * Example usage:
 * 
 * <pre>
 * InsertBuilder builder = InsertBuilder.builder("users")
 *         .columns("name", "email", "age")
 *         .values("John Doe", "test@gmail.com", 30);
 * String sql = builder.build();
 * System.out.println(sql);
 * // Output: INSERT INTO users (name, email, age) VALUES (?, ?, ?
 * </pre>
 * </p>
 * 
 * @author Kepter
 * @since 1.0
 * 
 */
public class InsertBuilder extends AbstractQueryBuilder {
    private List<String> columns;
    private List<Object> values;

    public InsertBuilder(String tableName) {
        super(tableName);
        this.values = new ArrayList<>();
    }

    public static InsertBuilder builder(String tableName) {
        return new InsertBuilder(tableName);
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
    public String createQuery() {
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
        if (RepositoryConfig.isPrintSql) {
            System.out.println("Generated Query: " + query);
        }
        return query;
    }

}
