package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Type;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class TypeRepository extends AbstractRepository<Type, Long> {
    public TypeRepository() {
        super(Type.class);
    }
}
