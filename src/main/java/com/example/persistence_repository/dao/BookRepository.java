package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.Book;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class BookRepository extends AbstractRepository<Book, Integer> {

    public BookRepository() {
        super(Book.class);
    }

}
