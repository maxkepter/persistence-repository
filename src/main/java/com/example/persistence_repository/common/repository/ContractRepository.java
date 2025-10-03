package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Contract;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class ContractRepository extends AbstractRepository<Contract, Long> {
    public ContractRepository() {
        super(Contract.class);
    }
}
