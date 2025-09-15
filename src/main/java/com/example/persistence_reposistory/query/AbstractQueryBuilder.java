package com.example.persistence_reposistory.query;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractQueryBuilder implements PrepareQueryBuilder {
    private List<Object> parameters;

    public AbstractQueryBuilder() {
        this.parameters = new ArrayList<>();
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    public List<Object> getParameters() {
        return parameters;
    }

}
