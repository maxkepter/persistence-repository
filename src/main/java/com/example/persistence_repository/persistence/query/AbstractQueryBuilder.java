package com.example.persistence_repository.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.example.persistence_repository.persistence.entity.EntityMeta;

/**
 * Abstract base class for building SQL queries.
 * Provides methods to set and get query parameters, and abstract methods
 * for building the SQL query string.
 * <p>
 * Subclasses must implement the abstract methods to provide specific
 * query-building logic.
 * </p>
 * 
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 */
public abstract class AbstractQueryBuilder<E> {
    private List<Object> parameters;
    protected EntityMeta<E> entityMeta;

    public AbstractQueryBuilder(EntityMeta<E> entityMeta) {
        this.entityMeta = entityMeta;
        this.parameters = new ArrayList<>();
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    abstract public String build();

    abstract public String build(boolean isPrintSql);

    abstract public String createQuery();
}
