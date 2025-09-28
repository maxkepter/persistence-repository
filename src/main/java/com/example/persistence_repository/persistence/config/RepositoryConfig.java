package com.example.persistence_repository.persistence.config;

public class RepositoryConfig {
    public static boolean isPrintSql = true;

    // MySQL cấu hình chính (sử dụng khi chạy thật)
    // Có thể chỉnh tham số useSSL=false & serverTimezone để tránh warning.
    public final static String DB_URL = "jdbc:mysql://localhost:3306/mydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public final static String USER = "root"; // TODO: thay bằng user thật nếu khác
    public final static String PASSWORD = "123456"; // TODO: thay bằng mật khẩu thật
    public final static String DRIVER = "com.mysql.cj.jdbc.Driver";

    /*
     * Cấu hình H2 trước đây (giữ lại để test nhanh nếu cần):
     * public final static String DB_URL =
     * "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1";
     * public final static String USER = "sa";
     * public final static String PASSWORD = "";
     * public final static String DRIVER = "org.h2.Driver";
     */
}
