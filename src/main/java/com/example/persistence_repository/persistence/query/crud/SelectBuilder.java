package com.example.persistence_repository.persistence.query.crud;

import java.util.ArrayList;
import java.util.List;

import com.example.persistence_repository.persistence.config.RepositoryConfig;
import com.example.persistence_repository.persistence.entity.EntityMeta;
import com.example.persistence_repository.persistence.query.AbstractQueryBuilder;
import com.example.persistence_repository.persistence.query.common.Order;

/**
 * A builder class for constructing SQL SELECT queries with various clauses
 * such as WHERE, ORDER BY, LIMIT, and OFFSET.
 * <p>
 * Example usage:
 * 
 * <pre>
 * SelectBuilder builder = SelectBuilder.builder("users")
 *         .columns("id", "name", "email")
 *         .distinct(true)
 *         .where("age > ?", 18)
 *         .orderBy(List.of(new Order("name", true)))
 *         .limit(10)
 *         .offset(5);
 * String sql = builder.build();
 * System.out.println(sql);
 * // Output: SELECT DISTINCT id, name, email FROM users WHERE age > ? ORDER BY
 * // name ASC LIMIT 10 OFFSET 5
 * </pre>
 * </p>
 * 
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 */
public class SelectBuilder<E> extends AbstractQueryBuilder<E> {

    private List<String> columns;
    private boolean isDistinct;
    private String whereClause;
    List<Order> orderByColumns;
    private String alias;
    private Integer limit;
    private Integer offset;

    public SelectBuilder(EntityMeta<E> entityMeta) {
        super(entityMeta);
        this.orderByColumns = new ArrayList<>();
    }

    public static <E> SelectBuilder<E> builder(EntityMeta<E> entityMeta) {
        if (entityMeta == null) {
            throw new IllegalArgumentException("EntityMeta cannot be null");
        }
        return new SelectBuilder<E>(entityMeta);
    }

    public SelectBuilder<E> alias(String alias) {
        this.alias = alias;
        return this;
    }

    public SelectBuilder<E> columns(String... columns) {
        this.columns = List.of(columns);
        return this;
    }

    public SelectBuilder<E> columns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public SelectBuilder<E> distinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
        return this;
    }

    public SelectBuilder<E> where(String whereClause, Object... params) {
        this.getParameters().addAll(List.of(params));
        this.whereClause = whereClause;
        return this;
    }

    public SelectBuilder<E> orderBy(List<Order> orderByColumns) {
        this.orderByColumns = orderByColumns;
        return this;
    }

    public SelectBuilder<E> limit(int limit) {
        this.limit = limit;
        return this;
    }

    public SelectBuilder<E> offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public String createQuery() {
        String tableName = entityMeta.getTableName();
        String tempAlias = alias;
        if (alias == null || alias.isEmpty()) {
            tempAlias = tableName;
        }
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("Table name is required for SELECT query");
        }
        StringBuilder query = new StringBuilder("SELECT ");
        if (isDistinct) {
            query.append("DISTINCT ");
        }
        if (columns == null || columns.isEmpty()) {
            query.append("*");
        } else {
            query.append(tempAlias).append(".").append(String.join(", ", columns));
        }
        query.append(" FROM ").append(tableName);
        query.append(" AS ").append(tempAlias);
        if (whereClause != null && !whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        if (orderByColumns != null && !orderByColumns.isEmpty()) {
            query.append(" ORDER BY ");
            for (int i = 0; i < orderByColumns.size(); i++) {
                Order order = orderByColumns.get(i);
                query.append(order.getColumn()).append(order.isAscending() ? " ASC" : " DESC");
                if (i < orderByColumns.size() - 1) {
                    query.append(", ");
                }
            }
        }
        if (limit != null) {
            query.append(" LIMIT ").append(limit);
        }
        if (offset != null) {
            query.append(" OFFSET ").append(offset);
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
        if (RepositoryConfig.isPrintSql) {
            System.out.println("Generated Query: " + query);
        }
        return query;
    }

}
