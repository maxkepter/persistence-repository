package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Request;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class RequestRepository extends AbstractRepository<Request, Long> {
    public RequestRepository() {
        super(Request.class);
    }
}
