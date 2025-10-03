package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Warehouse;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class WarehouseRepository extends AbstractRepository<Warehouse, Long> {
    public WarehouseRepository() {
        super(Warehouse.class);
    }
}
