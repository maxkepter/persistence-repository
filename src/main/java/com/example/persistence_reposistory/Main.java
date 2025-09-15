package com.example.persistence_reposistory;

import java.util.Arrays;
import java.util.List;

import com.example.persistence_reposistory.entity.User;
import com.example.persistence_reposistory.query.SelectBuilder;

public class Main {
    public static void main(String[] args) {
        // DBcontext db = new DBcontext();

        List<String> columns = Arrays.asList(User.class.getDeclaredFields()).stream().map((field) -> {
            field.setAccessible(true);
            return field.getName();
        }).toList();

        SelectBuilder builder = SelectBuilder.builder().columns(columns)
                .tableName(User.class.getSimpleName()).where("id=?", 1);
        String query = builder.build();
        List<Object> params = builder.getParameters();
        System.out.println(params);
        System.out.println(query);
    }
}