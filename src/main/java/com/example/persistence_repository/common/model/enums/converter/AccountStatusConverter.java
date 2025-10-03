package com.example.persistence_repository.common.model.enums.converter;

import com.example.persistence_repository.common.model.enums.AccountStatus;
import com.example.persistence_repository.persistence.entity.convert.EnumConverter;

public class AccountStatusConverter extends EnumConverter<AccountStatus> {
    public AccountStatusConverter() {
        super(AccountStatus.class);
    }

}
