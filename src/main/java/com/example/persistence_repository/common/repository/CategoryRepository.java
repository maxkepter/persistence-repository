package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.Category;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class CategoryRepository extends AbstractRepository<Category, Long> {
    public CategoryRepository() {
        super(Category.class);
    }
}
