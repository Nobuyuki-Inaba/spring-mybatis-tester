package com.github.nobuyuki.inaba.mybatis.test.example;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Entity for testing various data types with MyBatis.
 */
public class DataTypeEntity {
    private Long id;
    private Date testDate;
    private LocalDateTime testDateTime;
    private String testString;
    private String testEmptyString;
    private Integer testInteger;
    private Double testDouble;
    private BigDecimal testBigDecimal;

    public DataTypeEntity() {
    }

    public DataTypeEntity(Long id, Date testDate, LocalDateTime testDateTime, String testString, 
                          String testEmptyString, Integer testInteger, Double testDouble, 
                          BigDecimal testBigDecimal) {
        this.id = id;
        this.testDate = testDate;
        this.testDateTime = testDateTime;
        this.testString = testString;
        this.testEmptyString = testEmptyString;
        this.testInteger = testInteger;
        this.testDouble = testDouble;
        this.testBigDecimal = testBigDecimal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

    public LocalDateTime getTestDateTime() {
        return testDateTime;
    }

    public void setTestDateTime(LocalDateTime testDateTime) {
        this.testDateTime = testDateTime;
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public String getTestEmptyString() {
        return testEmptyString;
    }

    public void setTestEmptyString(String testEmptyString) {
        this.testEmptyString = testEmptyString;
    }

    public Integer getTestInteger() {
        return testInteger;
    }

    public void setTestInteger(Integer testInteger) {
        this.testInteger = testInteger;
    }

    public Double getTestDouble() {
        return testDouble;
    }

    public void setTestDouble(Double testDouble) {
        this.testDouble = testDouble;
    }

    public BigDecimal getTestBigDecimal() {
        return testBigDecimal;
    }

    public void setTestBigDecimal(BigDecimal testBigDecimal) {
        this.testBigDecimal = testBigDecimal;
    }

    @Override
    public String toString() {
        return "DataTypeEntity{" +
                "id=" + id +
                ", testDate=" + testDate +
                ", testDateTime=" + testDateTime +
                ", testString='" + testString + '\'' +
                ", testEmptyString='" + testEmptyString + '\'' +
                ", testInteger=" + testInteger +
                ", testDouble=" + testDouble +
                ", testBigDecimal=" + testBigDecimal +
                '}';
    }
}
