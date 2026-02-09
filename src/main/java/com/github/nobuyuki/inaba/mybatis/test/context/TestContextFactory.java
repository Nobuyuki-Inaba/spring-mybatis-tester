package com.github.nobuyuki.inaba.mybatis.test.context;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Strategy interface for creating test contexts.
 */
public interface TestContextFactory {
    /**
     * Creates a test context for the given test configuration.
     * 
     * @param annotation the MyBatisTest annotation
     * @param context the JUnit extension context
     * @return a configured TestContext
     * @throws Exception if context creation fails
     */
    TestContext createContext(MyBatisTest annotation, ExtensionContext context) throws Exception;
}
