package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Staff;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class StaffRepository extends AbstractRepository<Staff, Long> {
    public StaffRepository() {
        super(Staff.class);
    }
}
