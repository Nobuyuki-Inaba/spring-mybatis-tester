package com.github.nobuyuki.inaba.mybatis.test.database;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.Connection;

/**
 * Utility for executing SQL scripts during test setup.
 */
public final class ScriptExecutor {
    
    private ScriptExecutor() {
        // Utility class
    }
    
    /**
     * Runs SQL initialization scripts against a DataSource.
     * First attempts to load as a resource file, falls back to treating as inline SQL.
     * 
     * @param scripts array of script file paths or inline SQL
     * @param dataSource the DataSource to execute against
     * @throws Exception if script execution fails
     */
    public static void runInitScripts(String[] scripts, DataSource dataSource) throws Exception {
        if (scripts.length == 0) {
            return;
        }
        
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
