package com.example.persistence_repository.persistence.query.clause;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class ClauseBuilder {
    private final StringBuilder whereClause;
    private final List<Object> parameters;
    private boolean hasCondition;
    private String pendingOperator = null;

    public ClauseBuilder() {
        this.whereClause = new StringBuilder();
        this.parameters = new ArrayList<>();
        this.hasCondition = false;
    }

    public static ClauseBuilder builder() {
        return new ClauseBuilder();
    }

    public ClauseBuilder equal(String column, Object value) {
        return addCondition(column + " = ?", value);
    }

    public ClauseBuilder notEqual(String column, Object value) {
        return addCondition(column + " <> ?", value);
    }

    public ClauseBuilder greater(String column, Object value) {
        return addCondition(column + " > ?", value);
    }

    public ClauseBuilder less(String column, Object value) {
        return addCondition(column + " < ?", value);
    }

    public ClauseBuilder greaterOrEqual(String column, Object value) {
        return addCondition(column + " >= ?", value);
    }

    public ClauseBuilder lessOrEqual(String column, Object value) {
        return addCondition(column + " <= ?", value);
    }

    public ClauseBuilder like(String column, Object value) {
        return addCondition(column + " LIKE ?", value);
    }

    public ClauseBuilder in(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        StringJoiner sj = new StringJoiner(",", "(", ")");
        for (Object v : values) {
            sj.add("?");
            parameters.add(v);
        }
        return addRawCondition(column + " IN " + sj.toString());
    }

    public ClauseBuilder notIn(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        StringJoiner sj = new StringJoiner(",", "(", ")");
        for (Object v : values) {
            sj.add("?");
            parameters.add(v);
        }
        return addRawCondition(column + " NOT IN " + sj.toString());
    }

    public ClauseBuilder isNull(String column) {
        return addRawCondition(column + " IS NULL");
    }

    private ClauseBuilder addCondition(String sql, Object value) {
        if (value == null)
            return this; // bỏ qua null
        addRawCondition(sql);
        parameters.add(value);
        return this;
    }

    private ClauseBuilder addRawCondition(String sql) {
        if (hasCondition) {
            if (pendingOperator != null) {
                whereClause.append(" ").append(pendingOperator).append(" ");
                pendingOperator = null;
            } else {
                whereClause.append(" AND ");
            }

        }
        whereClause.append(sql);
        hasCondition = true;
        return this;
    }

    public ClauseBuilder and() {
        if (!hasCondition) {
            throw new IllegalStateException("Cannot use AND without a preceding condition");
        }
        pendingOperator = "AND";
        return this;
    }

    public ClauseBuilder or() {
        if (!hasCondition) {

            throw new IllegalStateException("Cannot use OR without a preceding condition");
        }
        pendingOperator = "OR";

        return this;
    }

    public ClauseBuilder group(Consumer<ClauseBuilder> consumer) {
        if (hasCondition) {
            whereClause.append(" AND ");
            hasCondition = false;
        }
        whereClause.append("(");
        ClauseBuilder nested = new ClauseBuilder();
        consumer.accept(nested);
        whereClause.append(nested.build());
        parameters.addAll(nested.getParameters());
        whereClause.append(")");
        hasCondition = true;
        return this;
    }

    public String build() {
        if (pendingOperator != null) {
            throw new IllegalStateException("Missing condition after AND/OR");
        }
        if (whereClause.length() == 0) {
            return "";
        }
        return whereClause.toString();
    }

    public List<Object> getParameters() {
        return parameters;
    }
}