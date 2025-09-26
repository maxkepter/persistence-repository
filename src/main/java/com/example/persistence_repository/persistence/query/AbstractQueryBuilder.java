package com.example.persistence_repository.persistence.query;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractQueryBuilder {
    private List<Object> parameters;

    protected String tableName;

    public AbstractQueryBuilder(String tableName) {
        this.tableName = tableName;
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
