package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.ProductWarehouse;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class ProductWarehouseRepository extends AbstractRepository<ProductWarehouse, Long> {
    public ProductWarehouseRepository() {
        super(ProductWarehouse.class);
    }
}
