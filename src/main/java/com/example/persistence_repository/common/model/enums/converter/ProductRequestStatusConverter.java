package com.example.persistence_repository.common.model.enums.converter;

import com.example.persistence_repository.common.model.enums.ProductRequestStatus;
import com.example.persistence_repository.persistence.entity.convert.EnumConverter;

public class ProductRequestStatusConverter extends EnumConverter<ProductRequestStatus> {
    public ProductRequestStatusConverter() {
        super(ProductRequestStatus.class);
    }
}
