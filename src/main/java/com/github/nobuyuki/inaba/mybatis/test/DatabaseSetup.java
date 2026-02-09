package com.github.nobuyuki.inaba.mybatis.test;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Utility class for setting up test databases.
 */
public class DatabaseSetup {

    /**
     * Creates a DataSource for testing.
     * <p>
     * Note: As of JDBC 4.0, explicit driver class loading is not required.
     * The DriverManager automatically discovers and loads drivers from the classpath
     * via the Service Provider mechanism.
     * 
     * @param url JDBC URL
     * @param username database username
     * @param password database password
     * @return configured DataSource
     */
    public static DataSource createDataSource(String url, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}
