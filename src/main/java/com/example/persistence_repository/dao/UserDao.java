package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.User;
import com.example.persistence_repository.persistence.repository.AbstractReposistory;

public class UserDao extends AbstractReposistory<User, Integer> {

    public UserDao() {
        super(User.class);
    }

}
