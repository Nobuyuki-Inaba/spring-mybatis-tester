package com.github.nobuyuki.inaba.mybatis.test.context;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import com.github.nobuyuki.inaba.mybatis.test.MyBatisTestConfiguration;
import com.github.nobuyuki.inaba.mybatis.test.database.ScriptExecutor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;

/**
 * Factory for creating Spring-based test contexts.
 */
public class SpringTestContextFactory implements TestContextFactory {
    
    @Override
    public TestContext createContext(MyBatisTest annotation, ExtensionContext context) throws Exception {
        TestContext testContext = new TestContext();
        
        ApplicationContext appContext = createSpringContext(annotation, context);
        testContext.setApplicationContext(appContext);
        testContext.setDataSource(appContext.getBean(DataSource.class));
        testContext.setSqlSessionFactory(appContext.getBean(SqlSessionFactory.class));
        
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
                ScriptExecutor.runInitScripts(annotation.initScripts(), ds);
            } catch (Exception e) {
                throw new RuntimeException("Failed to run init scripts in Spring context", e);
            }
        }
        
        return appContext;
    }
}
