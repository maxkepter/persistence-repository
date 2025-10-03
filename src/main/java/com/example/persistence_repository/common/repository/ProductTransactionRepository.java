package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.ProductTransaction;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class ProductTransactionRepository extends AbstractRepository<ProductTransaction, Long> {
    public ProductTransactionRepository() {
        super(ProductTransaction.class);
    }
}
