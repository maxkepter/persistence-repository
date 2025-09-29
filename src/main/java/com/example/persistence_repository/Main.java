package com.example.persistence_repository;

import java.lang.reflect.Member;
import java.util.List;

import com.example.persistence_repository.dao.AuthorRepository;
import com.example.persistence_repository.dao.BookRepository;
import com.example.persistence_repository.dao.MemberRepository;
import com.example.persistence_repository.entity.Author;
import com.example.persistence_repository.entity.Book;
import com.example.persistence_repository.persistence.query.clause.ClauseBuilder;
import com.example.persistence_repository.persistence.repository.CrudRepository;
import com.example.persistence_repository.persistence.repository.SimpleRepository;

public class Main {

    public static void main(String[] args) {
        selectTest();
        // System.out.println(resolveRepository(Author.class));
    }

    public static <R> CrudRepository<R, Object> resolveRepository(Class<R> targetType) {

        return (CrudRepository<R, Object>) new SimpleRepository<R, Object>(targetType);
    }

    public static void insertTest() {
        BookRepository bookRepository = new BookRepository();
        int count = bookRepository.count();
        System.out.println(count + " books in database.");
        Book book = new Book(count + 1, "Book Title " + (count + 1), "ISBN-000" + (count + 1), 2020 + (count + 1), 1);
        Book book2 = new Book(count + 2, "Book Title " + (count + 2), "ISBN-000" + (count + 2), 2020 + (count + 2), 1);
        Book book3 = new Book(count + 3, "Book Title " + (count + 3), "ISBN-000" + (count + 3), 2020 + (count + 3), 1);
        bookRepository.save(book);
        bookRepository.save(book2);
        bookRepository.save(book3);
        System.out.println("Inserted 3 books.");
        bookRepository.findAll().forEach((b) -> {
            System.out.println(b.getBookID() + " - " + b.getTitle());
        });

    }

    public static void selectTest() {
        AuthorRepository authorRepository = new AuthorRepository();
        authorRepository.findAll().forEach((a) -> {
            System.out.println(a.getAuthorID() + " - " + a.getName());
            System.out.println("Books:");
            System.out.println("---------------------");
            a.getBooks().forEach((b) -> {
                System.out.println(b.getBookID() + " - " + b.getTitle());
            });
        });
    }

}