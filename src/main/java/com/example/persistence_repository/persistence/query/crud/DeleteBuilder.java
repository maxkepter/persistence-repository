package com.example.persistence_repository.persistence.query.crud;

import java.util.List;

import com.example.persistence_repository.persistence.config.RepositoryConfig;
import com.example.persistence_repository.persistence.query.AbstractQueryBuilder;

/**
 * A builder class for constructing SQL DELETE queries with optional WHERE
 * clause.
 * <p>
 * Example usage:
 * 
 * <pre>
 * DeleteBuilder builder = DeleteBuilder.builder("users")
 *         .where("id = ?", 1);
 * String sql = builder.build();
 * System.out.println(sql);
 * // Output: DELETE FROM users WHERE id = ?
 * </pre>
 * </p>
 * 
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 */
public class DeleteBuilder extends AbstractQueryBuilder {

    public DeleteBuilder(String tableName) {
        super(tableName);
    }

    private String whereClause;

    public static DeleteBuilder builder(String tableName) {
        return new DeleteBuilder(tableName);
    }

    /**
     * Adds a WHERE clause to the DELETE query.
     * <p>
     * Example:
     * 
     * <pre>
     * DeleteBuilder builder = DeleteBuilder.builder("users")
     *         .where("id = ?", 1);
     * String sql = builder.build();
     * System.out.println(sql);
     * // Output: DELETE FROM users WHERE id = ?
     * </pre>
     * </p>
     * 
     * @param whereClause the WHERE clause (e.g., "id = ?")
     * @param values      the values to be set in the WHERE clause
     * @return the current DeleteBuilder instance for method chaining
     */
    public DeleteBuilder where(String whereClause, Object... values) {
        this.whereClause = whereClause;
        this.getParameters().addAll(List.of(values));
        return this;
    }

    /**
     * Creates the SQL DELETE query string based on the provided table name and
     * optional WHERE clause.
     * 
     * @return the constructed SQL DELETE query string
     * @throws IllegalStateException if the table name is not provided
     */
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

    /**
     * Builds the SQL DELETE query string.
     * 
     * @param isPrintSql if true, prints the generated SQL query to the console
     * @return the constructed SQL DELETE query string
     * 
     */
    @Override
    public String build(boolean isPrintSql) {
        String query = createQuery();
        if (isPrintSql) {
            System.out.println("Generated Query: " + query);
        }
        return query;
    }

    /**
     * Builds the SQL DELETE query string.
     * 
     * @return the constructed SQL DELETE query string
     */
    @Override
    public String build() {
        String query = createQuery();
        if (RepositoryConfig.isPrintSql) {
            System.out.println("Generated Query: " + query);
        }
        return query;
    }

}
