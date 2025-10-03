package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.AccountRequest;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class AccountRequestRepository extends AbstractRepository<AccountRequest, Long> {
    public AccountRequestRepository() {
        super(AccountRequest.class);
    }
}
