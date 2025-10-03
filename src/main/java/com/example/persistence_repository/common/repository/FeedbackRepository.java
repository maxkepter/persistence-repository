package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Feedback;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class FeedbackRepository extends AbstractRepository<Feedback, Long> {
    public FeedbackRepository() {
        super(Feedback.class);
    }
}
