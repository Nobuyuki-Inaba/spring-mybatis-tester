package com.github.nobuyuki.inaba.mybatis.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.extension.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * JUnit 5 extension that manages the MyBatis test lifecycle.
 * Handles database setup, transaction management, and dependency injection.
 */
public class MyBatisTestExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = 
        ExtensionContext.Namespace.create(MyBatisTestExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        MyBatisTest annotation = context.getRequiredTestClass().getAnnotation(MyBatisTest.class);
        Object testInstance = context.getRequiredTestInstance();
        
        TestContext testContext = createTestContext(annotation, context);
        context.getStore(NAMESPACE).put("testContext", testContext);
        
        // Load test data if specified
        if (!annotation.dataFile().isEmpty()) {
            XlsxDataLoader loader = new XlsxDataLoader();
            loader.loadData(annotation.dataFile(), testContext.getDataSource());
        }
        
        // Start transaction
        if (annotation.autoRollback()) {
            DataSourceTransactionManager txManager = new DataSourceTransactionManager(testContext.getDataSource());
            TransactionStatus tx = txManager.getTransaction(new DefaultTransactionDefinition());
            testContext.setTransaction(tx);
            testContext.setTransactionManager(txManager);
        }
        
        // Inject dependencies
        injectDependencies(testInstance, testContext);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        TestContext testContext = context.getStore(NAMESPACE).get("testContext", TestContext.class);
        
        if (testContext != null) {
            // Rollback transaction
            if (testContext.getTransaction() != null) {
                testContext.getTransactionManager().rollback(testContext.getTransaction());
            }
            
            // Close SQL session
            if (testContext.getSqlSession() != null) {
                testContext.getSqlSession().close();
            }
            
            // Close Spring context if used
            if (testContext.getApplicationContext() instanceof AnnotationConfigApplicationContext) {
                ((AnnotationConfigApplicationContext) testContext.getApplicationContext()).close();
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return SqlSession.class.isAssignableFrom(type) || 
               DataSource.class.isAssignableFrom(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        TestContext testContext = extensionContext.getStore(NAMESPACE).get("testContext", TestContext.class);
        Class<?> type = parameterContext.getParameter().getType();
        
        if (SqlSession.class.isAssignableFrom(type)) {
            return testContext.getSqlSession();
        } else if (DataSource.class.isAssignableFrom(type)) {
            return testContext.getDataSource();
        }
        return null;
    }

    private TestContext createTestContext(MyBatisTest annotation, ExtensionContext context) throws Exception {
        TestContext testContext = new TestContext();
        
        if (annotation.useSpring()) {
            // Use Spring context
            ApplicationContext appContext = createSpringContext(annotation, context);
            testContext.setApplicationContext(appContext);
            testContext.setDataSource(appContext.getBean(DataSource.class));
            testContext.setSqlSessionFactory(appContext.getBean(SqlSessionFactory.class));
        } else {
            // Standalone mode
            DataSource dataSource = DatabaseSetup.createDataSource(
                annotation.dbUrl(), 
                annotation.dbUsername(), 
                annotation.dbPassword()
            );
            testContext.setDataSource(dataSource);
            
            // Run init scripts
            runInitScripts(annotation.initScripts(), dataSource);
            
            // Create SqlSessionFactory
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(
                dataSource, 
                annotation.mappers().length > 0 ? annotation.mappers() : 
                    scanMappers(context.getRequiredTestClass())
            );
            testContext.setSqlSessionFactory(sqlSessionFactory);
        }
        
        // Create SQL session
        SqlSession sqlSession = testContext.getSqlSessionFactory().openSession();
        testContext.setSqlSession(sqlSession);
        
        return testContext;
    }

    private ApplicationContext createSpringContext(MyBatisTest annotation, ExtensionContext context) {
        // Create minimal Spring context with MyBatis support
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.register(MyBatisTestConfiguration.class);
        appContext.refresh();
        return appContext;
    }

    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource, Class<?>[] mappers) throws Exception {
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        
        // Register mappers
        for (Class<?> mapper : mappers) {
            config.addMapper(mapper);
        }
        
        org.apache.ibatis.mapping.Environment environment = 
            new org.apache.ibatis.mapping.Environment("test", 
                new org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory(), 
                dataSource);
        config.setEnvironment(environment);
        
        return new SqlSessionFactoryBuilder().build(config);
    }

    private Class<?>[] scanMappers(Class<?> testClass) {
        // Scan for mapper interfaces in the same package
        // For simplicity, return empty array - users should specify mappers
        return new Class<?>[0];
    }

    private void runInitScripts(String[] scripts, DataSource dataSource) throws Exception {
        if (scripts.length == 0) return;
        
        try (Connection conn = dataSource.getConnection()) {
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            
            for (String script : scripts) {
                try {
                    runner.runScript(Resources.getResourceAsReader(script));
                } catch (Exception e) {
                    // Try as direct SQL
                    runner.runScript(new StringReader(script));
                }
            }
        }
    }

    private void injectDependencies(Object testInstance, TestContext testContext) throws Exception {
        Class<?> clazz = testInstance.getClass();
        
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(org.springframework.beans.factory.annotation.Autowired.class)) {
                field.setAccessible(true);
                Object dependency = resolveDependency(field.getType(), testContext);
                if (dependency != null) {
                    field.set(testInstance, dependency);
                }
            }
        }
    }

    private Object resolveDependency(Class<?> type, TestContext testContext) {
        // Try Spring context first
        if (testContext.getApplicationContext() != null) {
            try {
                return testContext.getApplicationContext().getBean(type);
            } catch (Exception e) {
                // Fall through to MyBatis
            }
        }
        
        // Try MyBatis mapper
        try {
            return testContext.getSqlSession().getMapper(type);
        } catch (Exception e) {
            // Check for common types
            if (type == SqlSession.class) {
                return testContext.getSqlSession();
            } else if (type == DataSource.class) {
                return testContext.getDataSource();
            }
        }
        
        return null;
    }

    /**
     * Context object holding test resources.
     */
    private static class TestContext {
        private ApplicationContext applicationContext;
        private DataSource dataSource;
        private SqlSessionFactory sqlSessionFactory;
        private SqlSession sqlSession;
        private TransactionStatus transaction;
        private DataSourceTransactionManager transactionManager;

        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        public void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        public DataSource getDataSource() {
            return dataSource;
        }

        public void setDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public SqlSessionFactory getSqlSessionFactory() {
            return sqlSessionFactory;
        }

        public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
            this.sqlSessionFactory = sqlSessionFactory;
        }

        public SqlSession getSqlSession() {
            return sqlSession;
        }

        public void setSqlSession(SqlSession sqlSession) {
            this.sqlSession = sqlSession;
        }

        public TransactionStatus getTransaction() {
            return transaction;
        }

        public void setTransaction(TransactionStatus transaction) {
            this.transaction = transaction;
        }

        public DataSourceTransactionManager getTransactionManager() {
            return transactionManager;
        }

        public void setTransactionManager(DataSourceTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }
    }
}
