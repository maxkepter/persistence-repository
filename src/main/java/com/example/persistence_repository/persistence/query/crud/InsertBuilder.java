package com.example.persistence_repository.persistence.query.crud;

import java.util.List;

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

    public InsertBuilder(EntityMeta<E> entityMeta) {
        super(entityMeta);
    }

    private List<String> columns;
    private List<Object> values;

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
        this.values = values;
        return this;
    }

    public InsertBuilder<E> values(Object... values) {
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
