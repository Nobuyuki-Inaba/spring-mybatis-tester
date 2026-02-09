package com.github.nobuyuki.inaba.mybatis.test;

import com.github.nobuyuki.inaba.mybatis.test.database.DatabaseDriver;
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
        
        DatabaseDriver driver = DatabaseDriver.fromUrl(url);
        if (driver != null) {
            dataSource.setDriverClassName(driver.getDriverClassName());
        }
        
        return dataSource;
    }
}
