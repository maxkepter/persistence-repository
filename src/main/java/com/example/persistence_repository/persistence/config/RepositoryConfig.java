package com.example.persistence_repository.persistence.config;

import java.sql.Connection;

public class RepositoryConfig {
    private static Connection connection;

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
