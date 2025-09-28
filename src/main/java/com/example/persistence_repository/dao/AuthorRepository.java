package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.Author;
import com.example.persistence_repository.persistence.repository.AbstractReposistory;
import com.example.persistence_repository.persistence.repository.CrudReposistory;
import com.example.persistence_repository.persistence.repository.RepositoryRegistry;

public class AuthorRepository extends AbstractReposistory<Author, Integer> {

    public AuthorRepository() {
        super(Author.class);
        RepositoryRegistry.register(Author.class, this);
    }

    @Override
    protected <R> CrudReposistory<R, Object> resolveRepository(Class<R> targetType) {
        return RepositoryRegistry.get(targetType);
    }
}
