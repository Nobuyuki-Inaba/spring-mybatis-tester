package com.github.nobuyuki.inaba.mybatis.test.example;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Sample MyBatis mapper for testing.
 */
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users")
    List<User> findAll();

    @Insert("INSERT INTO users (id, name, email, age) VALUES (#{id}, #{name}, #{email}, #{age})")
    void insert(User user);

    @Insert("INSERT INTO users (name, email, age) VALUES (#{name}, #{email}, #{age})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertWithAutoId(User user);

    @Update("UPDATE users SET name = #{name}, email = #{email}, age = #{age} WHERE id = #{id}")
    void update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void delete(Long id);

    @Select("SELECT * FROM users WHERE age > #{age}")
    List<User> findByAgeGreaterThan(Integer age);
}
