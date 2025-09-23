package com.example.persistence_repository.dao;

import java.sql.Connection;

import com.example.persistence_repository.entity.User;
import com.example.persistence_repository.persistence.repository.AbstractReposistory;

public class UserDao extends AbstractReposistory<User, Integer> {

    public UserDao(Connection connection) {
        super(connection, User.class);
    }

}
