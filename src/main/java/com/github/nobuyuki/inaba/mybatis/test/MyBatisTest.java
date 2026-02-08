package com.github.nobuyuki.inaba.mybatis.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Main annotation for MyBatis unit tests.
 * Simplifies testing by providing automatic transaction management,
 * database setup, and optional Spring context support.
 * 
 * <p>Usage examples:</p>
 * <pre>
 * // Simple mode without Spring
 * {@code @MyBatisTest}
 * class UserMapperTest {
 *     {@code @Autowired}
 *     UserMapper mapper;
 * }
 * 
 * // With Spring context
 * {@code @MyBatisTest(useSpring = true)}
 * class UserServiceTest {
 *     {@code @Autowired}
 *     UserService service;
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(MyBatisTestExtension.class)
public @interface MyBatisTest {
    
    /**
     * Whether to use Spring context for dependency injection.
     * When true, full Spring Boot context is loaded.
     * When false (default), only MyBatis mappers are available.
     */
    boolean useSpring() default false;
    
    /**
     * Mapper classes to scan and register.
     * If empty, scans the test class package.
     */
    Class<?>[] mappers() default {};
    
    /**
     * Path to XLSX file with test data.
     * Data will be loaded before each test.
     */
    String dataFile() default "";
    
    /**
     * Whether to automatically rollback transactions after each test.
     * Default is true to keep tests isolated.
     */
    boolean autoRollback() default true;
    
    /**
     * SQL scripts to run before tests.
     * Useful for schema creation.
     */
    String[] initScripts() default {};
    
    /**
     * Database URL. Defaults to in-memory H2 database.
     */
    String dbUrl() default "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    
    /**
     * Database username.
     */
    String dbUsername() default "sa";
    
    /**
     * Database password.
     */
    String dbPassword() default "";
}
