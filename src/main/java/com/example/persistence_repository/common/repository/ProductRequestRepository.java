package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.ProductRequest;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class ProductRequestRepository extends AbstractRepository<ProductRequest, Long> {
    public ProductRequestRepository() {
        super(ProductRequest.class);
    }
}
