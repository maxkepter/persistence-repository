package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.Loan;
import com.example.persistence_repository.persistence.repository.AbstractReposistory;
import com.example.persistence_repository.persistence.repository.CrudReposistory;
import com.example.persistence_repository.persistence.repository.RepositoryRegistry;

public class LoanRepository extends AbstractReposistory<Loan, Integer> {

    public LoanRepository() {
        super(Loan.class);
        RepositoryRegistry.register(Loan.class, this);
    }

    @Override
    protected <R> CrudReposistory<R, Object> resolveRepository(Class<R> targetType) {
        return RepositoryRegistry.get(targetType);
    }
}
