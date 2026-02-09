-- Schema for data type testing
CREATE TABLE IF NOT EXISTS data_types (
    id BIGINT PRIMARY KEY,
    test_date DATE,
    test_date_time TIMESTAMP,
    test_string VARCHAR(255),
    test_empty_string VARCHAR(255),
    test_integer INT,
    test_double DOUBLE,
    test_big_decimal DECIMAL(19, 4)
);
