package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.RequestLog;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class RequestLogRepository extends AbstractRepository<RequestLog, Long> {
    public RequestLogRepository() {
        super(RequestLog.class);
    }
}
