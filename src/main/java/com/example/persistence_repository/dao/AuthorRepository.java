package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.Author;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class AuthorRepository extends AbstractRepository<Author, Integer> {

    public AuthorRepository() {
        super(Author.class);
    }

}
