package com.github.nobuyuki.inaba.mybatis.test.example;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test demonstrating XLSX data loading.
 * Note: This test is disabled as we don't have actual XLSX file.
 * In real usage, you would create testdata.xlsx with test data.
 */
@MyBatisTest(
    mappers = {UserMapper.class},
    initScripts = {"schema.sql"}
    // Uncomment when you have testdata.xlsx:
    // dataFile = "testdata.xlsx"
)
class UserMapperXlsxTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testDataLoadedFromXlsx() {
        // This test demonstrates how the library would work with XLSX data
        // In a real scenario, the data would be automatically loaded from testdata.xlsx
        
        // For now, we manually insert test data
        userMapper.insert(new User(1L, "Alice", "alice@test.com", 25));
        userMapper.insert(new User(2L, "Bob", "bob@test.com", 35));
        
        List<User> users = userMapper.findAll();
        assertEquals(2, users.size());
        
        // Verify the data
        User alice = userMapper.findById(1L);
        assertNotNull(alice);
        assertEquals("Alice", alice.getName());
        assertEquals("alice@test.com", alice.getEmail());
    }
}
