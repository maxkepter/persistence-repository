package com.example.persistence_repository.persistence.config;

import com.example.persistence_repository.PropertyLoader;

/**
 * Repository configuration loaded from application.properties
 * Keys:
 * - repository.url
 * - repository.username
 * - repository.password
 * - repository.driver-class-name
 * - repository.show-sql
 */
public class RepositoryConfig {

    public static final String DB_URL = PropertyLoader.get("repository.url", "");
    public static final String USER = PropertyLoader.get("repository.username", "");
    public static final String PASSWORD = PropertyLoader.get("repository.password", "");
    public static final String DRIVER = PropertyLoader.get("repository.driver-class-name", "");
    public static final boolean PRINT_SQL = PropertyLoader.getBoolean("repository.show-sql", true);

    private RepositoryConfig() {
    }
}
