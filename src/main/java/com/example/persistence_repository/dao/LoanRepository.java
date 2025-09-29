package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.Loan;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class LoanRepository extends AbstractRepository<Loan, Integer> {

    public LoanRepository() {
        super(Loan.class);
    }

}
