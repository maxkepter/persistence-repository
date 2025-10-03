package com.example.persistence_repository.common.model.enums.converter;

import com.example.persistence_repository.common.model.enums.TransactionStatus;
import com.example.persistence_repository.persistence.entity.convert.EnumConverter;

public class TransactionStatusConverter extends EnumConverter<TransactionStatus> {
    public TransactionStatusConverter() {
        super(TransactionStatus.class);
    }
}
