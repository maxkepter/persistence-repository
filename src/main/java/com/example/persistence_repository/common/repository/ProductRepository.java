package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Product;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class ProductRepository extends AbstractRepository<Product, Long> {
    public ProductRepository() {
        super(Product.class);
    }
}
