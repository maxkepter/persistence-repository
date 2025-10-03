package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

import java.time.LocalDateTime;

@Entity(tableName = "Feedback")
public class Feedback {
    @Key
    @Column(name = "FeedbackID", type = "BIGINT")
    private Long feedbackID;

    @Column(name = "Content", length = 255)
    private String content;

    @Column(name = "Rating", type = "INT")
    private Integer rating;

    @Column(name = "Response", length = 255)
    private String response;

    @Column(name = "FeedbackDate", type = "DATETIME", nullable = false)
    private LocalDateTime feedbackDate;

    @Column(name = "CustomerID", length = 100)
    private String customerID; // references Account.Username per schema

    @ManyToOne(joinColumn = "CustomerID", fetch = FetchMode.EAGER)
    private LazyReference<Account> account;

    public Long getFeedbackID() {
        return feedbackID;
    }

    public void setFeedbackID(Long feedbackID) {
        this.feedbackID = feedbackID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public LocalDateTime getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(LocalDateTime feedbackDate) {
        this.feedbackDate = feedbackDate;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public Account getAccount() {
        return account.get();
    }

    public void setAccount(Account account) {
        this.account.setValue(account);
    }
}
