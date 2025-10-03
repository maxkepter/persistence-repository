package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Role;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class RoleRepository extends AbstractRepository<Role, Long> {
    public RoleRepository() {
        super(Role.class);
    }
}
