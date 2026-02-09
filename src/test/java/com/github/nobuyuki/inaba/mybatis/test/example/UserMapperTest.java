package com.github.nobuyuki.inaba.mybatis.test.example;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test demonstrating the MyBatis testing library.
 * This test runs in standalone mode (without full Spring context).
 */
@MyBatisTest(
    mappers = {UserMapper.class},
    initScripts = {"schema.sql"}
)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testInsertAndFind() {
        // Given
        User user = new User(1L, "John Doe", "john@example.com", 30);
        
        // When
        userMapper.insert(user);
        User found = userMapper.findById(1L);
        
        // Then
        assertNotNull(found);
        assertEquals("John Doe", found.getName());
        assertEquals("john@example.com", found.getEmail());
        assertEquals(30, found.getAge());
    }

    @Test
    void testFindAll() {
        // Given
        userMapper.insert(new User(1L, "Alice", "alice@example.com", 25));
        userMapper.insert(new User(2L, "Bob", "bob@example.com", 35));
        
        // When
        List<User> users = userMapper.findAll();
        
        // Then
        assertEquals(2, users.size());
    }

    @Test
    void testUpdate() {
        // Given
        User user = new User(1L, "John Doe", "john@example.com", 30);
        userMapper.insert(user);
        
        // When
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        userMapper.update(user);
        
        // Then
        User updated = userMapper.findById(1L);
        assertEquals("Jane Doe", updated.getName());
        assertEquals("jane@example.com", updated.getEmail());
    }

    @Test
    void testDelete() {
        // Given
        User user = new User(1L, "John Doe", "john@example.com", 30);
        userMapper.insert(user);
        
        // When
        userMapper.delete(1L);
        
        // Then
        User deleted = userMapper.findById(1L);
        assertNull(deleted);
    }

    @Test
    void testFindByAgeGreaterThan() {
        // Given
        userMapper.insert(new User(1L, "Alice", "alice@example.com", 25));
        userMapper.insert(new User(2L, "Bob", "bob@example.com", 35));
        userMapper.insert(new User(3L, "Charlie", "charlie@example.com", 45));
        
        // When
        List<User> users = userMapper.findByAgeGreaterThan(30);
        
        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> u.getAge() > 30));
    }
}
