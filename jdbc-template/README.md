# JdbcTemplate Example

## Overview

This example demonstrates Spring's `JdbcTemplate` for SQL-based access to CockroachDB. JdbcTemplate provides a middle ground between raw JDBC and full ORM solutions like JPA, offering a clean API for executing SQL queries while maintaining full control over your SQL statements.

## Key Features

- Simplified JDBC operations with less boilerplate code
- Named parameter support for cleaner SQL queries
- Row mapping to Java objects using RowMapper interface
- Batch operations with automatic exception translation
- Integration with Spring's transaction management
- Built-in retry logic using Spring Retry annotations

## When to Use This Approach

Choose JdbcTemplate when you need:
- Direct SQL control without the complexity of raw JDBC
- Better performance than JPA for complex queries
- To work with existing database schemas or stored procedures
- Dynamic query generation
- To avoid the overhead of ORM mapping

## Usage Example

```java
@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    
    private final RowMapper<UserDTO> userRowMapper = (rs, rowNum) -> 
        new UserDTO(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("email")
        );
    
    @Retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root)")
    @Transactional
    public void saveUser(UserDTO user) {
        String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getId(), user.getName(), user.getEmail());
    }
    
    public List<UserDTO> findAllUsers() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }
    
    public void batchInsert(List<UserDTO> users) {
        String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UserDTO user = users.get(i);
                ps.setObject(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
            }
            
            @Override
            public int getBatchSize() {
                return users.size();
            }
        });
    }
}
```

## Configuration

Key application properties for JdbcTemplate:

```properties
# Database connection
spring.datasource.url=jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable&reWriteBatchedInserts=true

# Connection pool settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# Transaction isolation
spring.datasource.hikari.transaction-isolation=TRANSACTION_SERIALIZABLE

# Logging
logging.level.org.springframework.jdbc.core.JdbcTemplate=DEBUG
```

## Testing

Run tests for this module:
```bash
# Run all tests
./mvnw test -pl jdbc-template -DskipTests=false

# Run specific test class
./mvnw test -Dtest=UserServiceTest -pl jdbc-template -DskipTests=false

# Run specific test method
./mvnw test -Dtest=UserServiceRetryTest#testRetryOnSerializationFailure -pl jdbc-template -DskipTests=false
```

## Best Practices

### Query Writing
- Use parameterized queries to prevent SQL injection
- Prefer named parameters for complex queries using `NamedParameterJdbcTemplate`
- Keep SQL queries in constants or external files for maintainability

### Row Mapping
- Create reusable RowMapper implementations
- Use `BeanPropertyRowMapper` for simple POJOs
- Consider `ResultSetExtractor` for complex result processing

### Batch Operations
- Use `batchUpdate()` for bulk operations
- Set batch size to multiples of 128 for CockroachDB optimization
- Monitor memory usage for large batches

### Transaction Management
- Use `@Transactional` annotation for declarative transactions
- Ensure proper transaction boundaries for retry logic
- Consider read-only transactions for SELECT operations

### Error Handling
- Leverage Spring's `DataAccessException` hierarchy
- Implement custom exception translation when needed
- Always handle `DuplicateKeyException` for unique constraint violations

## Comparison with Other Approaches

| Aspect | DataSource | JdbcTemplate | JPA |
|--------|------------|--------------|-----|
| SQL Control | Full | Full | Limited |
| Boilerplate Code | High | Low | Minimal |
| Learning Curve | Steep | Moderate | Moderate |
| Type Safety | Low | Medium | High |
| Performance | Best | Excellent | Good |
| Spring Integration | Manual | Native | Native |

## Common Pitfalls

1. **Missing transactions**: Forgetting `@Transactional` for write operations
2. **N+1 queries**: Not using joins or batch fetching for related data
3. **Resource leaks**: Not properly handling large result sets
4. **Type mismatches**: Incorrect type casting in RowMapper implementations
5. **Retry scope**: Placing retry logic inside transaction boundaries

## Advanced Features

### Named Parameters
```java
NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(dataSource);
Map<String, Object> params = Map.of("name", "John", "email", "john@example.com");
namedTemplate.update("INSERT INTO users (name, email) VALUES (:name, :email)", params);
```

### Stored Procedures
```java
SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
    .withProcedureName("calculate_user_score");
Map<String, Object> result = jdbcCall.execute(Map.of("user_id", userId));
```

### Dynamic Queries
```java
public List<UserDTO> searchUsers(String name, String email) {
    StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
    List<Object> params = new ArrayList<>();
    
    if (name != null) {
        sql.append(" AND name LIKE ?");
        params.add("%" + name + "%");
    }
    if (email != null) {
        sql.append(" AND email = ?");
        params.add(email);
    }
    
    return jdbcTemplate.query(sql.toString(), userRowMapper, params.toArray());
}
```

## Additional Resources

- [Spring JdbcTemplate Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#jdbc-JdbcTemplate)
- [CockroachDB Best Practices](https://www.cockroachlabs.com/docs/stable/performance-best-practices-overview.html)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)