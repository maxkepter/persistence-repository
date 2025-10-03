package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.RoleFeature;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class RoleFeatureRepository extends AbstractRepository<RoleFeature, Long> {
    public RoleFeatureRepository() {
        super(RoleFeature.class);
    }
}
