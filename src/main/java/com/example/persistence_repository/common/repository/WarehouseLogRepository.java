package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.WarehouseLog;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class WarehouseLogRepository extends AbstractRepository<WarehouseLog, Long> {
    public WarehouseLogRepository() {
        super(WarehouseLog.class);
    }
}
