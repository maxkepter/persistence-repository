package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Customer;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class CustomerRepository extends AbstractRepository<Customer, Long> {
    public CustomerRepository() {
        super(Customer.class);
    }
}
