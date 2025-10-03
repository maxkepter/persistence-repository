package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.SpecificationType;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class SpecificationTypeRepository extends AbstractRepository<SpecificationType, Long> {
    public SpecificationTypeRepository() {
        super(SpecificationType.class);
    }
}
