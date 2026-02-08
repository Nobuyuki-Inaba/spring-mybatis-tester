package com.github.nobuyuki.inaba.mybatis.test;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Utility class for setting up test databases.
 */
public class DatabaseSetup {

    /**
     * Creates a DataSource for testing.
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
        
        // Set driver class based on URL
        if (url.startsWith("jdbc:h2:")) {
            dataSource.setDriverClassName("org.h2.Driver");
        } else if (url.startsWith("jdbc:mysql:")) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else if (url.startsWith("jdbc:postgresql:")) {
            dataSource.setDriverClassName("org.postgresql.Driver");
        }
        
        return dataSource;
    }
}
