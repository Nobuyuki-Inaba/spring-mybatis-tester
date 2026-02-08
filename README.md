# Spring MyBatis Tester

A simple and easy unit testing library for Spring Boot and MyBatis applications, focusing on developer experience and minimal configuration.

## Features

✨ **Simple & Easy** - Write tests with minimal boilerplate  
🔄 **Auto Rollback** - Automatic transaction management and rollback after each test  
📊 **XLSX Support** - Load test data from Excel files  
🔌 **Spring Integration** - Optional Spring context support  
🎯 **Flexible** - Works with or without Spring  
⚡ **Fast** - Uses in-memory H2 database by default

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>com.github.nobuyuki-inaba</groupId>
    <artifactId>spring-mybatis-tester</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### Basic Usage (Without Spring)

```java
@MyBatisTest(
    mappers = {UserMapper.class},
    initScripts = {"schema.sql"}
)
class UserMapperTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    void testFindUser() {
        User user = new User(1L, "John", "john@example.com", 30);
        userMapper.insert(user);
        
        User found = userMapper.findById(1L);
        assertEquals("John", found.getName());
    }
}
```

### With Spring Context

```java
@MyBatisTest(
    useSpring = true,
    initScripts = {"schema.sql"}
)
class UserServiceTest {
    
    @Autowired
    private UserService userService;  // Spring bean injection
    
    @Test
    void testUserService() {
        userService.createUser("John", "john@example.com");
        User user = userService.getUser(1L);
        assertNotNull(user);
    }
}
```

### With XLSX Test Data

Create a file `testdata.xlsx` with a sheet named `users`:

| id | name  | email           | age |
|----|-------|-----------------|-----|
| 1  | Alice | alice@test.com  | 25  |
| 2  | Bob   | bob@test.com    | 35  |

```java
@MyBatisTest(
    mappers = {UserMapper.class},
    initScripts = {"schema.sql"},
    dataFile = "testdata.xlsx"
)
class UserMapperTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    void testWithTestData() {
        List<User> users = userMapper.findAll();
        assertEquals(2, users.size());
    }
}
```

## Configuration Options

### @MyBatisTest Annotation

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `useSpring` | boolean | false | Enable full Spring context |
| `mappers` | Class<?>[] | [] | Mapper classes to register |
| `dataFile` | String | "" | Path to XLSX test data file |
| `autoRollback` | boolean | true | Rollback transactions after tests |
| `initScripts` | String[] | [] | SQL scripts to run before tests |
| `dbUrl` | String | jdbc:h2:mem:testdb | Database URL |
| `dbUsername` | String | sa | Database username |
| `dbPassword` | String | "" | Database password |

## Examples

### Example 1: Simple Mapper Test

```java
@MyBatisTest(mappers = {UserMapper.class}, initScripts = {"schema.sql"})
class SimpleTest {
    @Autowired
    private UserMapper mapper;
    
    @Test
    void test() {
        mapper.insert(new User(1L, "Test", "test@test.com", 20));
        assertEquals(1, mapper.findAll().size());
    }
}
```

### Example 2: Parameterized Tests

```java
@MyBatisTest(mappers = {UserMapper.class}, initScripts = {"schema.sql"})
class ParameterizedTest {
    @Autowired
    private UserMapper mapper;
    
    @ParameterizedTest
    @CsvSource({"Alice,25", "Bob,35", "Charlie,45"})
    void testMultipleUsers(String name, int age) {
        User user = new User(null, name, name.toLowerCase() + "@test.com", age);
        mapper.insert(user);
        
        List<User> found = mapper.findByAgeGreaterThan(age - 1);
        assertTrue(found.stream().anyMatch(u -> u.getName().equals(name)));
    }
}
```

### Example 3: With Service Layer

```java
@MyBatisTest(useSpring = true)
class ServiceTest {
    @Autowired
    private UserService userService;
    
    @Test
    void testServiceMethod() {
        userService.registerUser("John", "john@test.com");
        assertTrue(userService.isEmailRegistered("john@test.com"));
    }
}
```

## Benefits

### Before (Traditional Testing)

```java
@SpringBootTest
@Transactional
@Rollback
class UserMapperTest {
    @Autowired private DataSource dataSource;
    @Autowired private UserMapper userMapper;
    
    @BeforeEach
    void setup() throws Exception {
        ScriptUtils.executeSqlScript(
            dataSource.getConnection(), 
            new ClassPathResource("schema.sql")
        );
    }
    
    @Test
    void test() {
        // test code
    }
}
```

### After (With MyBatis Tester)

```java
@MyBatisTest(mappers = {UserMapper.class}, initScripts = {"schema.sql"})
class UserMapperTest {
    @Autowired private UserMapper userMapper;
    
    @Test
    void test() {
        // test code
    }
}
```

## Requirements

- Java 21 or higher
- Spring Boot 3.x (optional, for Spring mode)
- MyBatis 3.5.x
- JUnit 5

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
