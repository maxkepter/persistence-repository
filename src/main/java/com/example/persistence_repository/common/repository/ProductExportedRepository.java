package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.ProductExported;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class ProductExportedRepository extends AbstractRepository<ProductExported, Long> {
    public ProductExportedRepository() {
        super(ProductExported.class);
    }
}
