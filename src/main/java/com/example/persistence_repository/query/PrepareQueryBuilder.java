package com.example.persistence_repository.query;

import java.util.List;

public interface PrepareQueryBuilder {
    List<Object> getParameters();

    String build();
}
