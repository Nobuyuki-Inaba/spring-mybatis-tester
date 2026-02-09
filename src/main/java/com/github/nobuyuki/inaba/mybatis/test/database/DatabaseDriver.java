package com.github.nobuyuki.inaba.mybatis.test.database;

/**
 * Enumeration of supported database drivers with their JDBC URL patterns.
 */
public enum DatabaseDriver {
    H2("jdbc:h2:", "org.h2.Driver"),
    MYSQL("jdbc:mysql:", "com.mysql.cj.jdbc.Driver"),
    POSTGRESQL("jdbc:postgresql:", "org.postgresql.Driver");
    
    private final String urlPrefix;
    private final String driverClassName;
    
    DatabaseDriver(String urlPrefix, String driverClassName) {
        this.urlPrefix = urlPrefix;
        this.driverClassName = driverClassName;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    /**
     * Detects the appropriate database driver from a JDBC URL.
     * 
     * @param jdbcUrl the JDBC URL
     * @return the matching DatabaseDriver, or null if not found
     */
    public static DatabaseDriver fromUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return null;
        }
        
        for (DatabaseDriver driver : values()) {
            if (jdbcUrl.startsWith(driver.urlPrefix)) {
                return driver;
            }
        }
        
        return null;
    }
}
