package com.github.nobuyuki.inaba.mybatis.test.example.service;

import com.github.nobuyuki.inaba.mybatis.test.example.User;
import com.github.nobuyuki.inaba.mybatis.test.example.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Example service class that uses a MyBatis mapper.
 * This demonstrates testing service layer with the library.
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    public User createUser(String name, String email, Integer age) {
        User user = new User(null, name, email, age);
        userMapper.insertWithAutoId(user);
        return user;
    }

    public void updateUser(User user) {
        userMapper.update(user);
    }

    public void deleteUser(Long id) {
        userMapper.delete(id);
    }

    public List<User> findAdultUsers() {
        return userMapper.findByAgeGreaterThan(18);
    }

    public boolean isEmailRegistered(String email) {
        return userMapper.findAll().stream()
            .anyMatch(u -> u.getEmail().equals(email));
    }
}
