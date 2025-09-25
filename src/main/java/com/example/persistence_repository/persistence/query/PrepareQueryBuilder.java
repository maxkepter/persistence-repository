package com.example.persistence_repository.persistence.query;

import java.util.List;

public interface PrepareQueryBuilder {
    List<Object> getParameters();

    String build();

    String build(boolean isPrintSql);

    String createQuery();
}
