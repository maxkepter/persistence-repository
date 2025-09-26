package com.example.persistence_repository.persistence.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages database transactions using ThreadLocal to ensure thread safety.
 * <p>
 * This class provides methods to begin, commit, and rollback transactions,
 * as well as to retrieve the current connection associated with the thread.
 * </p>
 * 
 * <p>
 * Note: Best practice is to use this class within a try-catch block to handle
 * exceptions and ensure proper transaction management.
 * </p>
 * <p>
 * Note: Best practice is to use this in a service layer to manage transactions
 * across multiple
 * repository operations.
 * </p>
 * <p>
 * Example usage:
 * 
 * <pre>
 * TransactionManager.beginTransaction();
 * try {
 *     Connection conn = TransactionManager.getConnection();
 *     // Perform database operations
 *     TransactionManager.commit();
 * } catch (Exception e) {
 *     TransactionManager.rollback();
 * }
 * </pre>
 * 
 * </p>
 * <p>
 * Note: Ensure that connections are properly closed after use to prevent
 * resource leaks.
 * </p>
 * 
 */
public class TransactionManager {
    // ThreadLocal to hold the connection for each thread
    private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    // To keep track of transaction depth for nested transactions
    private static ThreadLocal<Integer> transactionDepthHolder = ThreadLocal.withInitial(() -> 0);

    /**
     * Begins a new transaction by setting auto-commit to false on the current
     * connection.
     * 
     * @throws SQLException if a database access error occurs
     */
    public static void beginTransaction() throws SQLException {

        if (transactionDepthHolder.get() < 1 && connectionHolder.get() == null) {
            Connection connection = DBcontext.createConnection();
            connection.setAutoCommit(false);
            connectionHolder.set(connection);
        }

        transactionDepthHolder.set(transactionDepthHolder.get() + 1);
    }

    /**
     * Commits the current transaction and closes the connection.
     * 
     * @throws SQLException
     */
    public static void commit() throws SQLException {
        int depth = transactionDepthHolder.get() - 1;
        transactionDepthHolder.set(depth);
        Connection connection = connectionHolder.get();
        if (connection == null) {
            throw new SQLException("No transaction to commit");
        }
        if (depth < 0) {
            throw new SQLException("No transaction to commit");
        }

        if (depth == 0) {
            connection.commit();
            connection.close();
            connectionHolder.remove();
        }

    }

    /**
     * Rolls back the current transaction and closes the connection.
     * 
     * @throws SQLException if a database access error occurs
     */
    public static void rollback() throws SQLException {
        int depth = transactionDepthHolder.get() - 1;
        transactionDepthHolder.set(depth);
        Connection connection = connectionHolder.get();
        if (connection == null) {
            throw new SQLException("No transaction to rollback");
        }
        if (depth < 0) {
            throw new SQLException("No transaction to rollback");
        }

        if (depth == 0) {
            connection.rollback();
            connection.close();
            connectionHolder.remove();
        }

    }

    /**
     * Retrieves the current connection associated with the thread.
     * If no connection exists, a new one is created.
     * 
     * @return the current Connection object
     */
    public static Connection getConnection() {
        Connection connection = connectionHolder.get();
        if (connection == null) {
            try {
                connection = DBcontext.createConnection();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            connectionHolder.set(connection);

        }
        return connection;
    }
}