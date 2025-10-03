package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.ProductSpecification;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class ProductSpecificationRepository extends AbstractRepository<ProductSpecification, Long> {
    public ProductSpecificationRepository() {
        super(ProductSpecification.class);
    }
}
