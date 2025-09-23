package com.example.persistence_repository;

import java.util.Arrays;
import java.util.List;

import com.example.persistence_repository.dao.UserDao;
import com.example.persistence_repository.entity.User;
import com.example.persistence_repository.persistence.query.AbstractQueryBuilder;
import com.example.persistence_repository.persistence.query.WhereClauseBuilder;
import com.example.persistence_repository.persistence.query.clause.ClauseTree;
import com.example.persistence_repository.persistence.query.crud.DeleteBuilder;
import com.example.persistence_repository.persistence.query.crud.InsertBuilder;
import com.example.persistence_repository.persistence.query.crud.SelectBuilder;
import com.example.persistence_repository.persistence.query.crud.UpdateBuilder;

public class Main {
    public static void main(String[] args) {
        DBcontext db = new DBcontext();
        UserDao userDao = new UserDao(db.getConnection());
        User user = new User(1, "name", "email");
        userDao.merge(user);
        // userDao.findAll().forEach(System.out::println);

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

    public static void where() {
        ClauseTree clauseTree = ClauseTree.and(ClauseTree.create("id = ?", 1), ClauseTree.create("name = ?", "test"));
        System.out.println(clauseTree.build());
        System.out.println(clauseTree.getParameters());
    }

}