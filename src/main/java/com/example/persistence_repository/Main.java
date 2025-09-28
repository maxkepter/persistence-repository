package com.example.persistence_repository;

import java.util.List;

import com.example.persistence_repository.dao.UserDao;
import com.example.persistence_repository.entity.User;
import com.example.persistence_repository.persistence.entity.EntityMeta;
import com.example.persistence_repository.persistence.query.common.Order;
import com.example.persistence_repository.persistence.query.common.Page;
import com.example.persistence_repository.persistence.query.common.PageRequest;
import com.example.persistence_repository.persistence.query.common.Sort;
import com.example.persistence_repository.persistence.query.crud.SelectBuilder;

public class Main {
    public static void main(String[] args) {

        testSelectBuilder();

    }

    public static void testSort() {
        PageRequest pageRequest = PageRequest.of(1, 20, Sort.by(Order.asc("email"), Order.desc("id")));
        UserDao userDao = new UserDao();
        Page<User> page = userDao.findAll(pageRequest);
        page.getContent().forEach(System.out::println);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
    }

    public static void testSelectBuilder() {
        SelectBuilder<User> builder = SelectBuilder.builder(EntityMeta.scanAnnotation(User.class))
                .columns("id", "name", "email")
                .distinct(true)
                .where("age > ?", 18)
                .orderBy(List.of(new Order("name", true)))
                .limit(10)
                .offset(5);
        String sql = builder.build();
        System.out.println(sql);
        // Output: SELECT DISTINCT id, name, email FROM users WHERE age > ? ORDER BY
        // name ASC LIMIT 10 OFFSET 5
    }

}