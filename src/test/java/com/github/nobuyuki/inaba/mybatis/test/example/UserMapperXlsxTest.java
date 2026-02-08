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
    initScripts = {"schema.sql"},
    dataFile = "testdata.xlsx"
)
class UserMapperXlsxTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testDataLoadedFromXlsx() {
        List<User> users = userMapper.findAll();
        assertEquals(3, users.size());
        
        // Verify the data
        User alice = userMapper.findById(1L);
        assertNotNull(alice);
        assertEquals("Alice", alice.getName());
        assertEquals("alice@example.com", alice.getEmail());
    }
}
