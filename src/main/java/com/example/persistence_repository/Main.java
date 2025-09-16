package com.example.persistence_repository;

import java.util.Arrays;
import java.util.List;

import com.example.persistence_repository.dao.UserDao;
import com.example.persistence_repository.entity.User;
import com.example.persistence_repository.query.AbstractQueryBuilder;
import com.example.persistence_repository.query.crud.DeleteBuilder;
import com.example.persistence_repository.query.crud.InsertBuilder;
import com.example.persistence_repository.query.crud.SelectBuilder;
import com.example.persistence_repository.query.crud.UpdateBuilder;

public class Main {
    public static void main(String[] args) {
        DBcontext db = new DBcontext();
        List<String> columns = Arrays.asList(User.class.getDeclaredFields()).stream().map((field) -> {
            field.setAccessible(true);
            return field.getName();
        }).toList();
        String tableName = User.class.getSimpleName();

        AbstractQueryBuilder builder = delete(columns, tableName);
        System.out.println("Query: " + builder.build());
        System.out.println("Parameters: " + builder.getParameters());
        // System.out.println(db.getConnection());
        // UserDao userDao = new UserDao(db.getConnection());
        // userDao.deleteById(10);
        // System.out.println(userDao.findAll());

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