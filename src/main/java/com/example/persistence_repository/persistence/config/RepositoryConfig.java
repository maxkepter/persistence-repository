package com.example.persistence_repository.persistence.config;

public class RepositoryConfig {
    public static boolean isPrintSql = true;

    // MySQL cấu hình chính (sử dụng khi chạy thật)
    // Có thể chỉnh tham số useSSL=false & serverTimezone để tránh warning.
    public final static String DB_URL = "jdbc:mysql://localhost:3306/mydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public final static String USER = "root";
    public final static String PASSWORD = "123456";
    public final static String DRIVER = "com.mysql.cj.jdbc.Driver";

}
