package com.example.persistence_repository.persistence.config;

import java.sql.Connection;

public class RepositoryConfig {
    public static Connection connection;
    public final static String DB_URL = "jdbc:mysql://localhost:3306/mydb";
    public final static String USER = "root";
    public final static String PASSWORD = "123456";

    public static Connection getConnection() {
        if (connection == null) {
            DBcontext dbcontext = new DBcontext();
            connection = dbcontext.getConnection();
        }
        return connection;
    }

    public static void setConnection(Connection conn) {
        connection = conn;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
