package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Specification;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class SpecificationRepository extends AbstractRepository<Specification, Long> {
    public SpecificationRepository() {
        super(Specification.class);
    }
}
