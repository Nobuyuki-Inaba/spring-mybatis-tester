package com.github.nobuyuki.inaba.mybatis.test.example;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test demonstrating the use of common test data loaded from XLSX file.
 * This test shows how multiple test methods can share the same dataset
 * loaded from testdata.xlsx at the start of each test.
 */
@MyBatisTest(
    mappers = {UserMapper.class},
    initScripts = {"schema.sql"},
    dataFile = "testdata.xlsx"
)
class UserMapperWithCommonDataTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testCommonDataIsLoaded() {
        // Common data should be automatically loaded from testdata.xlsx
        List<User> users = userMapper.findAll();
        
        // Verify we have the expected test data
        assertEquals(3, users.size(), "Should have 3 users from common test data");
    }

    @Test
    void testFindUserAliceFromCommonData() {
        // Alice should exist in common data (ID: 1)
        User alice = userMapper.findById(1L);
        
        assertNotNull(alice);
        assertEquals("Alice", alice.getName());
        assertEquals("alice@example.com", alice.getEmail());
        assertEquals(25, alice.getAge());
    }

    @Test
    void testFindUserBobFromCommonData() {
        // Bob should exist in common data (ID: 2)
        User bob = userMapper.findById(2L);
        
        assertNotNull(bob);
        assertEquals("Bob", bob.getName());
        assertEquals("bob@example.com", bob.getEmail());
        assertEquals(35, bob.getAge());
    }

    @Test
    void testFindUserCharlieFromCommonData() {
        // Charlie should exist in common data (ID: 3)
        User charlie = userMapper.findById(3L);
        
        assertNotNull(charlie);
        assertEquals("Charlie", charlie.getName());
        assertEquals("charlie@example.com", charlie.getEmail());
        assertEquals(45, charlie.getAge());
    }

    @Test
    void testQueryOnCommonData() {
        // Test querying the common data
        List<User> adults = userMapper.findByAgeGreaterThan(30);
        
        assertEquals(2, adults.size(), "Should have 2 users older than 30");
        assertTrue(adults.stream().allMatch(u -> u.getAge() > 30));
    }

    @Test
    void testModifyCommonDataInIsolation() {
        // Each test should start with fresh data due to auto-rollback
        // Delete one user
        userMapper.delete(1L);
        
        List<User> users = userMapper.findAll();
        assertEquals(2, users.size(), "Should have 2 users after deletion");
        
        // This deletion will be rolled back, so next test still has 3 users
    }

    @Test
    void testDataIsResetBetweenTests() {
        // This test verifies that the previous test's deletion was rolled back
        List<User> users = userMapper.findAll();
        
        assertEquals(3, users.size(), "Should still have 3 users - previous deletion was rolled back");
    }

    @Test
    void testInsertAdditionalDataOnTopOfCommonData() {
        // Start with common data (3 users) and add more
        userMapper.insert(new User(4L, "David", "david@example.com", 28));
        
        List<User> users = userMapper.findAll();
        assertEquals(4, users.size(), "Should have 4 users total");
        
        User david = userMapper.findById(4L);
        assertNotNull(david);
        assertEquals("David", david.getName());
    }
}
