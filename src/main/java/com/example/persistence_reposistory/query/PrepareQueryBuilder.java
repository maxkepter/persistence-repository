package com.example.persistence_reposistory.query;

import java.util.List;

public interface PrepareQueryBuilder extends QueryBuilder {
    List<Object> getParameters();
}
