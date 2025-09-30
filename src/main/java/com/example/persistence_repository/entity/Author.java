package com.example.persistence_repository.entity;

import java.sql.Date;
import java.util.List;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.annotation.OneToMany;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

@Entity(tableName = "Author")
public class Author {
    @Key
    @Column(type = "INT", name = "AuthorID")
    private Integer authorID;

    @Column(length = 100, nullable = false, name = "Name")
    private String name;

    @Column(type = "DATE", name = "BirthDate")
    private Date birthDate;

    @Column(length = 50, name = "Nationality")
    private String nationality;

    @OneToMany(mappedBy = "authorID", joinColumn = "authorID", fetch = FetchMode.LAZY)
    private List<Book> books;

    public Author() {
    }

    public Author(String name, Date birthDate, String nationality) {
        this.name = name;
        this.birthDate = birthDate;
        this.nationality = nationality;
    }

    @Override
    public String toString() {
        return "Author{" +
                "authorID=" + authorID +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", nationality='" + nationality + '\'' +
                '}';
    }

    public Integer getAuthorID() {
        return authorID;
    }

    public void setAuthorID(Integer authorID) {
        this.authorID = authorID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}
