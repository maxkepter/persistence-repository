package com.example.persistence_repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBcontext {
    private Connection connection;

    public DBcontext() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/mydb";

            String user = "root";
            String password = "123456";

            Connection conn = DriverManager.getConnection(url, user, password);
            this.connection = conn;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
