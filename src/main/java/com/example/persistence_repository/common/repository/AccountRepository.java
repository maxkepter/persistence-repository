package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Account;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class AccountRepository extends AbstractRepository<Account, String> {
    public AccountRepository() {
        super(Account.class);
    }
}
