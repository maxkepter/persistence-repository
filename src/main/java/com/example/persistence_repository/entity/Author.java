package com.example.persistence_repository.entity;

import java.sql.Date;
import java.util.List;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.annotation.OneToMany;
import com.example.persistence_repository.persistence.entity.FetchMode;

@Entity(tableName = "Author")
public class Author {
    @Key
    @Column(type = "INT")
    private Integer AuthorID; // dùng wrapper cho null safety khi insert auto increment

    @Column(length = 100, nullable = false)
    private String Name;

    @Column(type = "DATE")
    private Date BirthDate;

    @Column(length = 50)
    private String Nationality;

    // Quan hệ 1-n với Book (Book.AuthorID)
    @OneToMany(mappedBy = "AuthorID", joinColumn = "AuthorID", fetch = FetchMode.LAZY)
    private List<Book> books; // sẽ được gán LazyList trong mapResultSet

    public Author() {
    }

    public Author(String name, Date birthDate, String nationality) {
        this.Name = name;
        this.BirthDate = birthDate;
        this.Nationality = nationality;
    }

    public Integer getAuthorID() {
        return AuthorID;
    }

    public void setAuthorID(Integer authorID) {
        AuthorID = authorID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Date getBirthDate() {
        return BirthDate;
    }

    public void setBirthDate(Date birthDate) {
        BirthDate = birthDate;
    }

    public String getNationality() {
        return Nationality;
    }

    public void setNationality(String nationality) {
        Nationality = nationality;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return "Author{" +
                "AuthorID=" + AuthorID +
                ", Name='" + Name + '\'' +
                ", BirthDate=" + BirthDate +
                ", Nationality='" + Nationality + '\'' +
                '}';
    }
}
