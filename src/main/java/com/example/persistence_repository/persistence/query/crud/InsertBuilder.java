package com.example.persistence_repository.persistence.query.crud;

import java.util.List;
import java.util.ArrayList;

import com.example.persistence_repository.persistence.config.RepositoryConfig;
import com.example.persistence_repository.persistence.entity.EntityMeta;
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
 * @author Nguyen Anh Tu
 * @since 1.0
 * 
 */
public class InsertBuilder<E> extends AbstractQueryBuilder<E> {

    private List<String> columns;
    private List<Object> values;

    public InsertBuilder(EntityMeta<E> entityMeta) {
        super(entityMeta);
        values = new ArrayList<>();
    }

    public static <E> InsertBuilder<E> builder(EntityMeta<E> entityMeta) {
        return new InsertBuilder<E>(entityMeta);
    }

    public InsertBuilder<E> columns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public InsertBuilder<E> columns(String... columns) {
        this.columns = List.of(columns);
        return this;
    }

    public InsertBuilder<E> values(List<Object> values) {
        // Copy into mutable list to allow further additions
        this.values.addAll(values);
        return this;
    }

    public InsertBuilder<E> values(Object... values) {
        if (values == null || values.length == 0) {
            return this; // nothing to add
        }
        if (this.values == null) {
            this.values = new ArrayList<>();
        }
        this.values.addAll(List.of(values));
        return this;
    }

    @Override
    public String createQuery() {
        String tableName = entityMeta.getTableName();
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("Table name is required for INSERT query");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalStateException("At least one column is required for INSERT query");
        }
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException("Values must be provided for INSERT query");
        }
        int columnCount = columns.size();
        if (columnCount == 0) {
            throw new IllegalStateException("Column count must be greater than zero");
        }
        if (values.size() % columnCount != 0) {
            throw new IllegalStateException("Values count (" + values.size() + ") must be a multiple of columns count ("
                    + columnCount + ") for batch INSERT");
        }
        int rowCount = values.size() / columnCount;

        // Build placeholder for a single row e.g. "?, ?, ?"
        String singleRowPlaceholders = String.join(", ", columns.stream().map(c -> "?").toList());

        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(tableName).append(" (");
        query.append(String.join(", ", columns));
        query.append(") VALUES ");

        for (int i = 0; i < rowCount; i++) {
            if (i > 0) {
                query.append(", ");
            }
            query.append("(").append(singleRowPlaceholders).append(")");
        }

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
