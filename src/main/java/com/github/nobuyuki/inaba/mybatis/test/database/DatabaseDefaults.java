package com.github.nobuyuki.inaba.mybatis.test.database;

/**
 * Default database configuration values.
 * These can be overridden via system properties or annotation attributes.
 */
public final class DatabaseDefaults {
    
    public static final String DEFAULT_H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    public static final String DEFAULT_H2_USERNAME = "sa";
    public static final String DEFAULT_H2_PASSWORD = "";
    
    private DatabaseDefaults() {
        // Utility class
    }
}
