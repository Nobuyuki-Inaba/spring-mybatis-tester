package com.github.nobuyuki.inaba.mybatis.test.example;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Mapper for users_null_test table used in null handling tests.
 */
@Mapper
public interface UserNullTestMapper {

    @Select("SELECT * FROM users_null_test WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "email", column = "email"),
        @Result(property = "age", column = "age")
    })
    User findById(Long id);

    @Select("SELECT * FROM users_null_test")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "email", column = "email"),
        @Result(property = "age", column = "age")
    })
    List<User> findAll();
}
