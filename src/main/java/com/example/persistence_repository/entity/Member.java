package com.example.persistence_repository.entity;

import java.sql.Date;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;

@Entity(tableName = "Member")
public class Member {
    @Key
    @Column(type = "INT")
    private Integer MemberID;

    @Column(length = 100, nullable = false)
    private String FullName;

    @Column(length = 100, nullable = false, unique = true)
    private String Email;

    @Column(type = "DATE")
    private Date JoinDate; // default CURRENT_DATE trên DB

    public Member() {
    }

    public Member(String fullName, String email) {
        this.FullName = fullName;
        this.Email = email;
    }

    public Integer getMemberID() {
        return MemberID;
    }

    public void setMemberID(Integer memberID) {
        MemberID = memberID;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public Date getJoinDate() {
        return JoinDate;
    }

    public void setJoinDate(Date joinDate) {
        JoinDate = joinDate;
    }

    @Override
    public String toString() {
        return "Member{" +
                "MemberID=" + MemberID +
                ", FullName='" + FullName + '\'' +
                ", Email='" + Email + '\'' +
                ", JoinDate=" + JoinDate +
                '}';
    }
}
