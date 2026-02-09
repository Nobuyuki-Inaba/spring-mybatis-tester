package com.github.nobuyuki.inaba.mybatis.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.extension.*;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
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
        
        if (!annotation.dataFile().isEmpty()) {
            XlsxDataLoader loader = new XlsxDataLoader();
            loader.loadData(annotation.dataFile(), testContext.getDataSource());
        }
        
        if (annotation.autoRollback()) {
            DataSourceTransactionManager txManager = new DataSourceTransactionManager(testContext.getDataSource());
            TransactionStatus tx = txManager.getTransaction(new DefaultTransactionDefinition());
            testContext.setTransaction(tx);
            testContext.setTransactionManager(txManager);
        }
        
        injectDependencies(testInstance, testContext);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        TestContext testContext = context.getStore(NAMESPACE).get("testContext", TestContext.class);
        
        if (testContext != null) {
            if (testContext.getTransaction() != null) {
                testContext.getTransactionManager().rollback(testContext.getTransaction());
            }
            
            if (testContext.getSqlSession() != null) {
                testContext.getSqlSession().close();
            }
            
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
            ApplicationContext appContext = createSpringContext(annotation, context);
            testContext.setApplicationContext(appContext);
            testContext.setDataSource(appContext.getBean(DataSource.class));
            testContext.setSqlSessionFactory(appContext.getBean(SqlSessionFactory.class));
        } else {
            DataSource dataSource = DatabaseSetup.createDataSource(
                annotation.dbUrl(), 
                annotation.dbUsername(), 
                annotation.dbPassword()
            );
            testContext.setDataSource(dataSource);
            
            runInitScripts(annotation.initScripts(), dataSource);
            
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(
                dataSource, 
                annotation.mappers().length > 0 ? annotation.mappers() : 
                    scanMappers(context.getRequiredTestClass())
            );
            testContext.setSqlSessionFactory(sqlSessionFactory);
        }
        
        SqlSession sqlSession = testContext.getSqlSessionFactory().openSession();
        testContext.setSqlSession(sqlSession);
        
        return testContext;
    }

    private ApplicationContext createSpringContext(MyBatisTest annotation, ExtensionContext context) {
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        
        appContext.register(MyBatisTestConfiguration.class);
        
        String testPackage = context.getRequiredTestClass().getPackage().getName();
        appContext.scan(testPackage);
        
        if (annotation.mappers().length > 0) {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) appContext.getBeanFactory();
            
            for (Class<?> mapperClass : annotation.mappers()) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder
                    .genericBeanDefinition(MapperFactoryBean.class)
                    .addConstructorArgValue(mapperClass)
                    .addPropertyReference("sqlSessionFactory", "sqlSessionFactory");
                
                String beanName = mapperClass.getSimpleName().substring(0, 1).toLowerCase() + 
                                 mapperClass.getSimpleName().substring(1);
                beanFactory.registerBeanDefinition(beanName, builder.getBeanDefinition());
            }
        }
        
        appContext.refresh();

        if (annotation.initScripts().length > 0) {
            try {
                DataSource ds = appContext.getBean(DataSource.class);
                runInitScripts(annotation.initScripts(), ds);
            } catch (Exception e) {
                throw new RuntimeException("Failed to run init scripts in Spring context", e);
            }
        }
        
        return appContext;
    }

    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource, Class<?>[] mappers) throws Exception {
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        
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
        if (testContext.getApplicationContext() != null) {
            try {
                return testContext.getApplicationContext().getBean(type);
            } catch (Exception e) {
            }
        }
        
        try {
            return testContext.getSqlSession().getMapper(type);
        } catch (Exception e) {
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
