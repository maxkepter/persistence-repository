package com.example.persistence_repository.persistence.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DBcontext {
    private static Connection connection;

    /**
     * Get a connection, create one if it doesn't exist or is closed
     * 
     * @return Connection use for readonly operations
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
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

    /**
     * Create a new database connection using the configuration parameters.
     * 
     * @return a new Connection object use for transactional operations
     * @throws SQLException if a database access error occurs
     */
    public static Connection createConnection() throws SQLException {
        String url = RepositoryConfig.DB_URL;
        String user = RepositoryConfig.USER;
        String password = RepositoryConfig.PASSWORD;
        try {
            Class.forName(RepositoryConfig.DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection conn = DriverManager.getConnection(url, user, password);
        // Khởi tạo schema tùy theo loại DB (tùy chọn, không bắt buộc nếu đã tự tạo sẵn)
        if (url.startsWith("jdbc:h2:")) {
            runSchema(conn, "schema-h2.sql");
        } else if (url.startsWith("jdbc:mysql:")) {
            runSchema(conn, "schema-mysql.sql");
        }
        return conn;
    }

    private static void runSchema(Connection conn, String classpathResource) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = DBcontext.class.getClassLoader();
        }
        try (InputStream is = cl.getResourceAsStream(classpathResource)) {
            if (is == null)
                return; // nothing to run
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            String[] statements = sb.toString().split(";\\s*\n");
            try (Statement st = conn.createStatement()) {
                for (String sql : statements) {
                    String trimmed = sql.trim();
                    if (trimmed.isEmpty())
                        continue;
                    st.execute(trimmed);
                }
            }
        } catch (Exception e) {
            // log and continue (schema init failure should surface via later SQL errors)
            e.printStackTrace();
        }
    }

}
