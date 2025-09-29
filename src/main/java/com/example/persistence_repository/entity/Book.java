package com.example.persistence_repository.entity;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.annotation.ManyToOne;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

@Entity(tableName = "Book")
public class Book {
    @Key
    @Column(type = "INT")
    private Integer BookID;

    @Column(length = 200, nullable = false)
    private String Title;

    @Column(length = 20, nullable = false, unique = true)
    private String ISBN;

    @Column(type = "INT")
    private Integer PublishedYear;

    @ManyToOne(joinColumn = "AuthorID", fetch = FetchMode.EAGER)
    private Author author;

    @Column(type = "INT", nullable = false)
    private Integer AuthorID;

    public Book(Integer bookID, String title, String iSBN, Integer publishedYear, Integer authorID) {
        BookID = bookID;
        Title = title;
        ISBN = iSBN;
        PublishedYear = publishedYear;
        AuthorID = authorID;
    }

    public Book() {
    }

    public Integer getBookID() {
        return BookID;
    }

    public void setBookID(Integer bookID) {
        BookID = bookID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String iSBN) {
        ISBN = iSBN;
    }

    public Integer getPublishedYear() {
        return PublishedYear;
    }

    public void setPublishedYear(Integer publishedYear) {
        PublishedYear = publishedYear;
    }

    public Integer getAuthorID() {
        return AuthorID;
    }

    public void setAuthorID(Integer authorID) {
        AuthorID = authorID;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Book{" +
                "BookID=" + BookID +
                ", Title='" + Title + '\'' +
                ", ISBN='" + ISBN + '\'' +
                ", PublishedYear=" + PublishedYear +
                ", AuthorID=" + AuthorID +
                '}';
    }
}
