package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.Book;
import com.example.persistence_repository.persistence.repository.AbstractReposistory;
import com.example.persistence_repository.persistence.repository.CrudReposistory;
import com.example.persistence_repository.persistence.repository.RepositoryRegistry;
import com.example.persistence_repository.persistence.query.clause.ClauseBuilder;

public class BookRepository extends AbstractReposistory<Book, Integer> {

    public BookRepository() {
        super(Book.class);
        RepositoryRegistry.register(Book.class, this);
    }

    @Override
    protected <R> CrudReposistory<R, Object> resolveRepository(Class<R> targetType) {
        return RepositoryRegistry.get(targetType);
    }

    public void deleteByAuthorId(int authorId) {
        // Sử dụng ClauseBuilder đúng cách để xây dựng điều kiện AuthorID = ?
        ClauseBuilder clause = ClauseBuilder.builder().equal("AuthorID", authorId);
        deleteWithCondition(clause);
    }
}
