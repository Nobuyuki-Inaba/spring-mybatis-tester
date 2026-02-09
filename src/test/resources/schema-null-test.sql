-- Schema for null handling test database
-- This schema allows NULL values in email to test null vs empty string handling
-- Uses a different table name to avoid conflicts with other tests
CREATE TABLE IF NOT EXISTS users_null_test (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),  -- Allows NULL
    age INT
);
