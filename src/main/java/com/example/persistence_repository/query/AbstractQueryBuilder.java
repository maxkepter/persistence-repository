package com.example.persistence_repository.query;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractQueryBuilder implements PrepareQueryBuilder {
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

}
