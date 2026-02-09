package com.github.nobuyuki.inaba.mybatis.test.context;

import com.github.nobuyuki.inaba.mybatis.test.DatabaseSetup;
import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.Connection;

/**
 * Factory for creating standalone (non-Spring) test contexts.
 */
public class StandaloneTestContextFactory implements TestContextFactory {
    
    @Override
    public TestContext createContext(MyBatisTest annotation, ExtensionContext context) throws Exception {
        TestContext testContext = new TestContext();
        
        DataSource dataSource = DatabaseSetup.createDataSource(
            annotation.dbUrl(), 
            annotation.dbUsername(), 
            annotation.dbPassword()
        );
        testContext.setDataSource(dataSource);
        
        runInitScripts(annotation.initScripts(), dataSource);
        
        SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(
            dataSource, 
            annotation.mappers().length > 0 ? annotation.mappers() : new Class<?>[0]
        );
        testContext.setSqlSessionFactory(sqlSessionFactory);
        
        SqlSession sqlSession = sqlSessionFactory.openSession();
        testContext.setSqlSession(sqlSession);
        
        return testContext;
    }
    
    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource, Class<?>[] mappers) {
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
}
