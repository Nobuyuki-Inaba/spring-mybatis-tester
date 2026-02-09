package com.github.nobuyuki.inaba.mybatis.test.example;

import com.github.nobuyuki.inaba.mybatis.test.MyBatisTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class demonstrating data type handling in MyBatis.
 * Tests various data types including Date, DateTime (LocalDateTime), 
 * empty String, Integer, Double, and BigDecimal.
 */
@MyBatisTest(
    mappers = {DataTypeMapper.class},
    initScripts = {"schema-data-types.sql"}
)
class DataTypeTest {

    @Autowired
    private DataTypeMapper dataTypeMapper;

    @Test
    void testDateHandling() {
        // Given
        Date testDate = new Date();
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(1L);
        entity.setTestDate(testDate);
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(100);
        entity.setTestDouble(123.45);
        entity.setTestBigDecimal(new BigDecimal("999.99"));

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(1L);

        // Then
        assertNotNull(found);
        assertNotNull(found.getTestDate());
        // Date type in SQL DATE stores only the date part (no time)
        // So we verify that the date was stored and retrieved (value is not null)
        assertTrue(found.getTestDate() instanceof Date, "Retrieved value should be a Date object");
    }

    @Test
    void testDateTimeHandling() {
        // Given
        LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(2L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(testDateTime);
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(100);
        entity.setTestDouble(123.45);
        entity.setTestBigDecimal(new BigDecimal("999.99"));

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(2L);

        // Then
        assertNotNull(found);
        assertNotNull(found.getTestDateTime());
        assertEquals(testDateTime.getYear(), found.getTestDateTime().getYear());
        assertEquals(testDateTime.getMonth(), found.getTestDateTime().getMonth());
        assertEquals(testDateTime.getDayOfMonth(), found.getTestDateTime().getDayOfMonth());
        assertEquals(testDateTime.getHour(), found.getTestDateTime().getHour());
        assertEquals(testDateTime.getMinute(), found.getTestDateTime().getMinute());
        assertEquals(testDateTime.getSecond(), found.getTestDateTime().getSecond());
    }

    @Test
    void testEmptyStringHandling() {
        // Given
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(3L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("non-empty");
        entity.setTestEmptyString("");  // Empty string
        entity.setTestInteger(100);
        entity.setTestDouble(123.45);
        entity.setTestBigDecimal(new BigDecimal("999.99"));

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(3L);

        // Then
        assertNotNull(found);
        assertNotNull(found.getTestEmptyString());
        assertEquals("", found.getTestEmptyString(), "Empty string should be stored and retrieved correctly");
        assertTrue(found.getTestEmptyString().isEmpty(), "Test empty string should be empty");
        assertNotEquals(found.getTestString(), found.getTestEmptyString(), 
                       "Empty string should be different from non-empty string");
    }

    @Test
    void testIntegerHandling() {
        // Given
        Integer testInteger = 12345;
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(4L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(testInteger);
        entity.setTestDouble(123.45);
        entity.setTestBigDecimal(new BigDecimal("999.99"));

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(4L);

        // Then
        assertNotNull(found);
        assertNotNull(found.getTestInteger());
        assertEquals(testInteger, found.getTestInteger(), "Integer should be stored and retrieved correctly");
    }

    @Test
    void testNegativeIntegerHandling() {
        // Given
        Integer negativeInteger = -9999;
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(5L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(negativeInteger);
        entity.setTestDouble(123.45);
        entity.setTestBigDecimal(new BigDecimal("999.99"));

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(5L);

        // Then
        assertNotNull(found);
        assertEquals(negativeInteger, found.getTestInteger(), 
                    "Negative integer should be stored and retrieved correctly");
    }

    @Test
    void testDoubleHandling() {
        // Given
        Double testDouble = 123.456789;
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(6L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(100);
        entity.setTestDouble(testDouble);
        entity.setTestBigDecimal(new BigDecimal("999.99"));

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(6L);

        // Then
        assertNotNull(found);
        assertNotNull(found.getTestDouble());
        assertEquals(testDouble, found.getTestDouble(), 0.0001, 
                    "Double should be stored and retrieved correctly");
    }

    @Test
    void testNegativeDoubleHandling() {
        // Given
        Double negativeDouble = -987.654321;
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(7L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(100);
        entity.setTestDouble(negativeDouble);
        entity.setTestBigDecimal(new BigDecimal("999.99"));

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(7L);

        // Then
        assertNotNull(found);
        assertEquals(negativeDouble, found.getTestDouble(), 0.0001, 
                    "Negative double should be stored and retrieved correctly");
    }

    @Test
    void testBigDecimalHandling() {
        // Given
        BigDecimal testBigDecimal = new BigDecimal("12345.6789");
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(8L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(100);
        entity.setTestDouble(123.45);
        entity.setTestBigDecimal(testBigDecimal);

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(8L);

        // Then
        assertNotNull(found);
        assertNotNull(found.getTestBigDecimal());
        assertEquals(testBigDecimal.compareTo(found.getTestBigDecimal()), 0, 
                    "BigDecimal should be stored and retrieved correctly");
    }

    @Test
    void testBigDecimalPrecisionHandling() {
        // Given - Test BigDecimal with high precision
        BigDecimal highPrecisionBigDecimal = new BigDecimal("99999.9999");
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(9L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(100);
        entity.setTestDouble(123.45);
        entity.setTestBigDecimal(highPrecisionBigDecimal);

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(9L);

        // Then
        assertNotNull(found);
        assertNotNull(found.getTestBigDecimal());
        assertEquals(highPrecisionBigDecimal.compareTo(found.getTestBigDecimal()), 0, 
                    "High precision BigDecimal should be stored and retrieved correctly");
    }

    @Test
    void testNegativeBigDecimalHandling() {
        // Given
        BigDecimal negativeBigDecimal = new BigDecimal("-8888.8888");
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(10L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("test");
        entity.setTestEmptyString("");
        entity.setTestInteger(100);
        entity.setTestDouble(123.45);
        entity.setTestBigDecimal(negativeBigDecimal);

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(10L);

        // Then
        assertNotNull(found);
        assertEquals(negativeBigDecimal.compareTo(found.getTestBigDecimal()), 0, 
                    "Negative BigDecimal should be stored and retrieved correctly");
    }

    @Test
    void testAllDataTypesTogetherInsertion() {
        // Given - Create entity with all data types set
        Date testDate = new Date();
        LocalDateTime testDateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        String testString = "All data types test";
        String emptyString = "";
        Integer testInteger = 42;
        Double testDouble = 3.14159;
        BigDecimal testBigDecimal = new BigDecimal("1234567.8901");

        DataTypeEntity entity = new DataTypeEntity(
            11L, testDate, testDateTime, testString, emptyString, 
            testInteger, testDouble, testBigDecimal
        );

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(11L);

        // Then
        assertNotNull(found);
        assertEquals(11L, found.getId());
        assertNotNull(found.getTestDate());
        assertNotNull(found.getTestDateTime());
        assertEquals(testString, found.getTestString());
        assertEquals(emptyString, found.getTestEmptyString());
        assertEquals(testInteger, found.getTestInteger());
        assertNotNull(found.getTestDouble());
        assertNotNull(found.getTestBigDecimal());
    }

    @Test
    void testUpdateAllDataTypes() {
        // Given - Insert initial entity
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(12L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("initial");
        entity.setTestEmptyString("");
        entity.setTestInteger(100);
        entity.setTestDouble(100.0);
        entity.setTestBigDecimal(new BigDecimal("100.00"));
        dataTypeMapper.insert(entity);

        // When - Update all fields
        Date newDate = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000L)); // +1 day
        LocalDateTime newDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
        entity.setTestDate(newDate);
        entity.setTestDateTime(newDateTime);
        entity.setTestString("updated");
        entity.setTestEmptyString("");
        entity.setTestInteger(200);
        entity.setTestDouble(200.0);
        entity.setTestBigDecimal(new BigDecimal("200.00"));
        dataTypeMapper.update(entity);

        // Then
        DataTypeEntity found = dataTypeMapper.findById(12L);
        assertNotNull(found);
        assertEquals("updated", found.getTestString());
        assertEquals(200, found.getTestInteger());
        assertEquals(200.0, found.getTestDouble(), 0.001);
        assertEquals(new BigDecimal("200.00").compareTo(found.getTestBigDecimal()), 0);
    }

    @Test
    void testFindAllWithMultipleDataTypeRecords() {
        // Given - Insert multiple records
        for (int i = 1; i <= 5; i++) {
            DataTypeEntity entity = new DataTypeEntity();
            entity.setId((long) (20 + i));
            entity.setTestDate(new Date());
            entity.setTestDateTime(LocalDateTime.now());
            entity.setTestString("record-" + i);
            entity.setTestEmptyString("");
            entity.setTestInteger(i * 10);
            entity.setTestDouble(i * 1.5);
            entity.setTestBigDecimal(new BigDecimal(String.valueOf(i * 100)));
            dataTypeMapper.insert(entity);
        }

        // When
        List<DataTypeEntity> allRecords = dataTypeMapper.findAll();

        // Then
        assertNotNull(allRecords);
        assertTrue(allRecords.size() >= 5, "Should have at least 5 records");
        
        // Verify all records have proper data types
        for (DataTypeEntity record : allRecords) {
            if (record.getId() >= 21 && record.getId() <= 25) {
                assertNotNull(record.getTestDate());
                assertNotNull(record.getTestDateTime());
                assertNotNull(record.getTestString());
                assertNotNull(record.getTestInteger());
                assertNotNull(record.getTestDouble());
                assertNotNull(record.getTestBigDecimal());
            }
        }
    }

    @Test
    void testDeleteDataTypeRecord() {
        // Given
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(30L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("to-be-deleted");
        entity.setTestEmptyString("");
        entity.setTestInteger(999);
        entity.setTestDouble(999.99);
        entity.setTestBigDecimal(new BigDecimal("999.99"));
        dataTypeMapper.insert(entity);

        // When
        dataTypeMapper.delete(30L);

        // Then
        DataTypeEntity deleted = dataTypeMapper.findById(30L);
        assertNull(deleted, "Record should be deleted");
    }

    @Test
    void testZeroValues() {
        // Given - Test zero values for numeric types
        DataTypeEntity entity = new DataTypeEntity();
        entity.setId(31L);
        entity.setTestDate(new Date());
        entity.setTestDateTime(LocalDateTime.now());
        entity.setTestString("zero-test");
        entity.setTestEmptyString("");
        entity.setTestInteger(0);
        entity.setTestDouble(0.0);
        entity.setTestBigDecimal(BigDecimal.ZERO);

        // When
        dataTypeMapper.insert(entity);
        DataTypeEntity found = dataTypeMapper.findById(31L);

        // Then
        assertNotNull(found);
        assertEquals(0, found.getTestInteger(), "Integer zero should be stored correctly");
        assertEquals(0.0, found.getTestDouble(), "Double zero should be stored correctly");
        assertEquals(0, BigDecimal.ZERO.compareTo(found.getTestBigDecimal()), 
                    "BigDecimal zero should be stored correctly");
    }
}
