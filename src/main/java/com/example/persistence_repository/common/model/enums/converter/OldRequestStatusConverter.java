package com.example.persistence_repository.common.model.enums.converter;

import com.example.persistence_repository.common.model.enums.OldRequestStatus;
import com.example.persistence_repository.persistence.entity.convert.EnumConverter;

public class OldRequestStatusConverter extends EnumConverter<OldRequestStatus> {
    public OldRequestStatusConverter() {
        super(OldRequestStatus.class);
    }
}
