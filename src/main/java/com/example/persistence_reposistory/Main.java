package com.example.persistence_reposistory;

import java.util.Arrays;
import java.util.List;

import com.example.persistence_reposistory.entity.User;
import com.example.persistence_reposistory.query.AbstractQueryBuilder;
import com.example.persistence_reposistory.query.DeleteBuilder;
import com.example.persistence_reposistory.query.InsertBuilder;
import com.example.persistence_reposistory.query.SelectBuilder;
import com.example.persistence_reposistory.query.UpdateBuilder;

public class Main {
    public static void main(String[] args) {
        // DBcontext db = new DBcontext();
        List<String> columns = Arrays.asList(User.class.getDeclaredFields()).stream().map((field) -> {
            field.setAccessible(true);
            return field.getName();
        }).toList();
        String tableName = User.class.getSimpleName();

        AbstractQueryBuilder builder = delete(columns, tableName);
        System.out.println("Query: " + builder.build());
        System.out.println("Parameters: " + builder.getParameters());

    }

    public static AbstractQueryBuilder select(List<String> columns, String tableName) {

        SelectBuilder builder = SelectBuilder.builder().columns(columns)
                .tableName(tableName).where("id=?", 1);
        return builder;
    }

    public static AbstractQueryBuilder insert(List<String> columns) {
        InsertBuilder builder = InsertBuilder.builder().columns(columns)
                .tableName(User.class.getSimpleName()).values(1, "John", "test");

        return builder;
    }

    public static AbstractQueryBuilder update(List<String> columns, String tableName) {
        UpdateBuilder builder = UpdateBuilder.builder().tableName(tableName).set("id", 1).set("name",
                "lmao");
        return builder;
    }

    public static AbstractQueryBuilder delete(List<String> columns, String tableName) {
        DeleteBuilder builder = DeleteBuilder.builder().tableName(tableName);
        return builder;
    }

}