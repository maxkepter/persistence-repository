package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Feature;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class FeatureRepository extends AbstractRepository<Feature, Long> {
    public FeatureRepository() {
        super(Feature.class);
    }
}
