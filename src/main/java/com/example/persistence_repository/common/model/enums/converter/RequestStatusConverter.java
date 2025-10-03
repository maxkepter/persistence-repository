package com.example.persistence_repository.common.model.enums.converter;

import com.example.persistence_repository.common.model.enums.RequestStatus;
import com.example.persistence_repository.persistence.entity.convert.EnumConverter;

public class RequestStatusConverter extends EnumConverter<RequestStatus> {
    public RequestStatusConverter() {
        super(RequestStatus.class);
    }
}
