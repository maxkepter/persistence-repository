package com.example.persistence_repository.entity;

import java.sql.Date;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.annotation.ManyToOne;
import com.example.persistence_repository.persistence.entity.FetchMode;
import com.example.persistence_repository.persistence.entity.LazyReference;

@Entity(tableName = "Loan")
public class Loan {
    @Key
    @Column(type = "INT")
    private Integer LoanID;

    @Column(type = "INT", nullable = false)
    private Integer BookID;

    @Column(type = "INT", nullable = false)
    private Integer MemberID;

    @Column(type = "DATE")
    private Date LoanDate;

    @Column(type = "DATE")
    private Date ReturnDate;

    @ManyToOne(joinColumn = "BookID", fetch = FetchMode.LAZY)
    private LazyReference<Book> book; // chuyển sang LazyReference để mapper gán trực tiếp

    @ManyToOne(joinColumn = "MemberID", fetch = FetchMode.EAGER)
    private Member member; // eager

    public Loan() {
    }

    public Loan(Integer bookID, Integer memberID) {
        this.BookID = bookID;
        this.MemberID = memberID;
    }

    public Integer getLoanID() {
        return LoanID;
    }

    public void setLoanID(Integer loanID) {
        LoanID = loanID;
    }

    public Integer getBookID() {
        return BookID;
    }

    public void setBookID(Integer bookID) {
        BookID = bookID;
    }

    public Integer getMemberID() {
        return MemberID;
    }

    public void setMemberID(Integer memberID) {
        MemberID = memberID;
    }

    public Date getLoanDate() {
        return LoanDate;
    }

    public void setLoanDate(Date loanDate) {
        LoanDate = loanDate;
    }

    public Date getReturnDate() {
        return ReturnDate;
    }

    public void setReturnDate(Date returnDate) {
        ReturnDate = returnDate;
    }

    public Book getBook() {
        return book == null ? null : book.get();
    }

    public void setBook(LazyReference<Book> book) {
        this.book = book;
    }

    public boolean isBookLoaded() {
        return book != null && book.isLoaded();
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "LoanID=" + LoanID +
                ", BookID=" + BookID +
                ", MemberID=" + MemberID +
                ", LoanDate=" + LoanDate +
                ", ReturnDate=" + ReturnDate +
                ", BookLoaded=" + isBookLoaded() +
                '}';
    }
}
