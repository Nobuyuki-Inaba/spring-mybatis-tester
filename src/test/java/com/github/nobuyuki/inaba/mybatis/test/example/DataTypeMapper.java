package com.github.nobuyuki.inaba.mybatis.test.example;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Mapper for testing various data types with MyBatis.
 */
@Mapper
public interface DataTypeMapper {

    @Select("SELECT * FROM data_types WHERE id = #{id}")
    DataTypeEntity findById(Long id);

    @Select("SELECT * FROM data_types")
    List<DataTypeEntity> findAll();

    @Insert("INSERT INTO data_types (id, test_date, test_date_time, test_string, test_empty_string, " +
            "test_integer, test_double, test_big_decimal) " +
            "VALUES (#{id}, #{testDate}, #{testDateTime}, #{testString}, #{testEmptyString}, " +
            "#{testInteger}, #{testDouble}, #{testBigDecimal})")
    void insert(DataTypeEntity entity);

    @Update("UPDATE data_types SET test_date = #{testDate}, test_date_time = #{testDateTime}, " +
            "test_string = #{testString}, test_empty_string = #{testEmptyString}, " +
            "test_integer = #{testInteger}, test_double = #{testDouble}, test_big_decimal = #{testBigDecimal} " +
            "WHERE id = #{id}")
    void update(DataTypeEntity entity);

    @Delete("DELETE FROM data_types WHERE id = #{id}")
    void delete(Long id);
}
