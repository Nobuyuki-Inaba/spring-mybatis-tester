package com.github.nobuyuki.inaba.mybatis.test.example;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test demonstrating null vs empty string handling in XLSX data loading.
 * This test shows how the library handles:
 * 1. NULL values (blank cells or missing cells)
 * 2. Empty strings (cells with "" value)
 * 3. Explicit NULL markers (cells with "NULL" string)
 * 4. Actual string values
 */
@MyBatisTest(
    mappers = {UserNullTestMapper.class},
    initScripts = {"schema-null-test.sql"},
    dataFile = "testdata-null-handling.xlsx"
)
class UserMapperNullHandlingTest {

    @Autowired
    private UserNullTestMapper userMapper;

    @Test
    void testNullVsEmptyStringHandling() {
        // Test user with ID 10: has null email (blank cell in XLSX)
        User userWithNull = userMapper.findById(10L);
        assertNotNull(userWithNull);
        assertNull(userWithNull.getEmail(), "Email should be NULL for blank cell");
        
        // Test user with ID 11: has empty string email (cell with "" in XLSX)
        User userWithEmptyString = userMapper.findById(11L);
        assertNotNull(userWithEmptyString);
        assertEquals("", userWithEmptyString.getEmail(), "Email should be empty string for cell with \"\"");
        
        // Test user with ID 12: has actual email value
        User userWithValue = userMapper.findById(12L);
        assertNotNull(userWithValue);
        assertEquals("test@example.com", userWithValue.getEmail(), "Email should have actual value");
        
        // Test user with ID 13: has explicit NULL marker (string "NULL")
        User userWithNullMarker = userMapper.findById(13L);
        assertNotNull(userWithNullMarker);
        assertNull(userWithNullMarker.getEmail(), "Email should be NULL for cell with \"NULL\" string");
        
        // Test user with ID 14: has explicit NULL marker (string "null" lowercase)
        User userWithLowercaseNull = userMapper.findById(14L);
        assertNotNull(userWithLowercaseNull);
        assertNull(userWithLowercaseNull.getEmail(), "Email should be NULL for cell with \"null\" string (case-insensitive)");
    }
    
    @Test
    void testDifferentiatingNullFromEmptyString() {
        // This test demonstrates that NULL and empty string are distinct values
        
        // Count users with NULL email (should be 3: IDs 10, 13, 14)
        long nullCount = userMapper.findAll().stream()
            .filter(u -> u.getEmail() == null)
            .count();
        assertEquals(3, nullCount, "Should have 3 users with NULL email");
        
        // Count users with empty string email (should be 1: ID 11)
        long emptyCount = userMapper.findAll().stream()
            .filter(u -> u.getEmail() != null && u.getEmail().isEmpty())
            .count();
        assertEquals(1, emptyCount, "Should have 1 user with empty string email");
        
        // Count users with actual email values (should be 1: ID 12)
        long valueCount = userMapper.findAll().stream()
            .filter(u -> u.getEmail() != null && !u.getEmail().isEmpty())
            .count();
        assertEquals(1, valueCount, "Should have 1 user with actual email value");
    }
}
