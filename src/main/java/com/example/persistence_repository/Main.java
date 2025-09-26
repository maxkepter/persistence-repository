package com.example.persistence_repository;

import java.util.List;

import com.example.persistence_repository.dao.UserDao;
import com.example.persistence_repository.entity.User;
import com.example.persistence_repository.persistence.query.AbstractQueryBuilder;
import com.example.persistence_repository.persistence.query.common.Order;
import com.example.persistence_repository.persistence.query.common.Page;
import com.example.persistence_repository.persistence.query.common.PageRequest;
import com.example.persistence_repository.persistence.query.common.Sort;
import com.example.persistence_repository.persistence.query.crud.DeleteBuilder;
import com.example.persistence_repository.persistence.query.crud.InsertBuilder;
import com.example.persistence_repository.persistence.query.crud.SelectBuilder;
import com.example.persistence_repository.persistence.query.crud.UpdateBuilder;

public class Main {
    public static void main(String[] args) {

    }

    public static void testSort() {
        PageRequest pageRequest = PageRequest.of(1, 20, Sort.by(Order.asc("email"), Order.desc("id")));
        UserDao userDao = new UserDao();
        Page<User> page = userDao.findAll(pageRequest);
        page.getContent().forEach(System.out::println);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
    }

    public static AbstractQueryBuilder select(List<String> columns, String tableName) {

        SelectBuilder builder = SelectBuilder.builder(tableName).columns(columns)
                .where("id=?", 1);
        return builder;
    }

    public static AbstractQueryBuilder insert(List<String> columns, String tableName) {
        InsertBuilder builder = InsertBuilder.builder(tableName).columns(columns)
                .values(1, "John", "test");

        return builder;
    }

    public static AbstractQueryBuilder update(List<String> columns, String tableName) {
        UpdateBuilder builder = UpdateBuilder.builder(tableName).set("id", 1).set("name",
                "lmao");
        return builder;
    }

    public static AbstractQueryBuilder delete(List<String> columns, String tableName) {
        DeleteBuilder builder = DeleteBuilder.builder(tableName);
        return builder;
    }

}