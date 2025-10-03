package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.ProductContract;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class ProductContractRepository extends AbstractRepository<ProductContract, Long> {
    public ProductContractRepository() {
        super(ProductContract.class);
    }
}
