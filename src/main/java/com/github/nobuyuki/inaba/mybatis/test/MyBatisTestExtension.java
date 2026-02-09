package com.github.nobuyuki.inaba.mybatis.test;

import com.github.nobuyuki.inaba.mybatis.test.context.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.extension.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.lang.reflect.Field;

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
        
        if (testContext == null) {
            return;
        }
        
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
        TestContextFactory factory = annotation.useSpring() 
            ? new SpringTestContextFactory() 
            : new StandaloneTestContextFactory();
        
        return factory.createContext(annotation, context);
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
}
