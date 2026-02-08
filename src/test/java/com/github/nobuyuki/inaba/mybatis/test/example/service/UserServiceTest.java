package com.github.nobuyuki.inaba.mybatis.test.example.service;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import com.github.nobuyuki.inaba.mybatis.test.example.User;
import com.github.nobuyuki.inaba.mybatis.test.example.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test demonstrating service layer testing with MyBatis Tester.
 * This shows how developers can test service classes that use MyBatis mappers.
 */
@MyBatisTest(
    useSpring = true,
    mappers = {UserMapper.class},
    initScripts = {"schema.sql"}
)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateAndGetUser() {
        // When
        User created = userService.createUser("John Doe", "john@example.com", 30);
        
        // Then
        assertNotNull(created.getId());
        
        User found = userService.getUserById(created.getId());
        assertNotNull(found);
        assertEquals("John Doe", found.getName());
        assertEquals("john@example.com", found.getEmail());
        assertEquals(30, found.getAge());
    }

    @Test
    void testGetAllUsers() {
        // Given
        userService.createUser("Alice", "alice@test.com", 25);
        userService.createUser("Bob", "bob@test.com", 35);
        
        // When
        List<User> users = userService.getAllUsers();
        
        // Then
        assertEquals(2, users.size());
    }

    @Test
    void testUpdateUser() {
        // Given
        User user = userService.createUser("John", "john@test.com", 30);
        
        // When
        user.setName("Jane");
        user.setEmail("jane@test.com");
        userService.updateUser(user);
        
        // Then
        User updated = userService.getUserById(user.getId());
        assertEquals("Jane", updated.getName());
        assertEquals("jane@test.com", updated.getEmail());
    }

    @Test
    void testDeleteUser() {
        // Given
        User user = userService.createUser("John", "john@test.com", 30);
        Long id = user.getId();
        
        // When
        userService.deleteUser(id);
        
        // Then
        User deleted = userService.getUserById(id);
        assertNull(deleted);
    }

    @Test
    void testFindAdultUsers() {
        // Given
        userService.createUser("Child", "child@test.com", 15);
        userService.createUser("Adult1", "adult1@test.com", 25);
        userService.createUser("Adult2", "adult2@test.com", 35);
        
        // When
        List<User> adults = userService.findAdultUsers();
        
        // Then
        assertEquals(2, adults.size());
        assertTrue(adults.stream().allMatch(u -> u.getAge() > 18));
    }

    @Test
    void testIsEmailRegistered() {
        // Given
        userService.createUser("John", "john@test.com", 30);
        
        // When/Then
        assertTrue(userService.isEmailRegistered("john@test.com"));
        assertFalse(userService.isEmailRegistered("notregistered@test.com"));
    }
}
