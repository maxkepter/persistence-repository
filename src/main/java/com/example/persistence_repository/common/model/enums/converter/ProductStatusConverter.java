package com.example.persistence_repository.common.model.enums.converter;

import com.example.persistence_repository.common.model.enums.ProductStatus;
import com.example.persistence_repository.persistence.entity.convert.EnumConverter;

public class ProductStatusConverter extends EnumConverter<ProductStatus> {
    public ProductStatusConverter() {
        super(ProductStatus.class);
    }
}
