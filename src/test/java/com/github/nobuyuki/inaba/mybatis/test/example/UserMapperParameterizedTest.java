package com.github.nobuyuki.inaba.mybatis.test.example;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test demonstrating parameterized tests with MyBatis Tester.
 */
@MyBatisTest(
    mappers = {UserMapper.class},
    initScripts = {"schema.sql"}
)
class UserMapperParameterizedTest {

    @Autowired
    private UserMapper userMapper;

    @ParameterizedTest
    @CsvSource({
        "Alice,25,alice@test.com",
        "Bob,35,bob@test.com",
        "Charlie,45,charlie@test.com"
    })
    void testInsertMultipleUsers(String name, int age, String email) {
        // Given
        User user = new User(null, name, email, age);
        
        // When
        userMapper.insertWithAutoId(user);
        
        // Then
        List<User> users = userMapper.findAll();
        assertTrue(users.stream().anyMatch(u -> 
            u.getName().equals(name) && 
            u.getEmail().equals(email) && 
            u.getAge() == age
        ));
    }

    @ParameterizedTest
    @CsvSource({
        "20,3",  // All 3 users are > 20
        "25,2",  // Bob(35) and Charlie(45) are > 25
        "30,2",  // Bob(35) and Charlie(45) are > 30
        "35,1",  // Only Charlie(45) is > 35
        "40,1"   // Only Charlie(45) is > 40
    })
    void testFindByAgeGreaterThan(int ageThreshold, int expectedCount) {
        // Given - insert test data
        userMapper.insert(new User(1L, "Alice", "alice@test.com", 25));
        userMapper.insert(new User(2L, "Bob", "bob@test.com", 35));
        userMapper.insert(new User(3L, "Charlie", "charlie@test.com", 45));
        
        // When
        List<User> users = userMapper.findByAgeGreaterThan(ageThreshold);
        
        // Then
        assertEquals(expectedCount, users.size());
        assertTrue(users.stream().allMatch(u -> u.getAge() > ageThreshold));
    }
}
