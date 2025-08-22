# Datasource Example

## Overview

This example demonstrates direct JDBC access to CockroachDB using the `jakarta.sql.DataSource` interface. This low-level approach provides maximum control over database interactions and is ideal when you need to work directly with JDBC primitives or implement custom database access patterns not covered by higher-level abstractions.

## Key Features

- Direct JDBC connection management via DataSource
- Manual transaction handling with Connection objects
- PreparedStatement usage for parameterized queries
- Batch operations for improved performance
- Custom retry logic implementation using Spring Retry
- Connection pooling with HikariCP

## When to Use This Approach

Choose the DataSource pattern when you need:
- Fine-grained control over database connections and transactions
- To implement custom batching strategies
- To work with database-specific features not exposed by JPA/Hibernate
- Maximum performance with minimal abstraction overhead
- To integrate with legacy JDBC code

## Usage Example

```java
@Service
public class UserService {
    private final DataSource dataSource;
    
    public void insertUsers(List<UserDTO> users) {
        String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (UserDTO user : users) {
                ps.setObject(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
                ps.addBatch();
            }
            
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            // Handle exception
        }
    }
}
```

## Configuration

Key application properties for DataSource configuration:

```properties
# Connection pool settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# CockroachDB-specific optimizations
spring.datasource.hikari.data-source-properties.reWriteBatchedInserts=true
spring.datasource.hikari.transaction-isolation=TRANSACTION_SERIALIZABLE

# Connection URL (varies by profile)
spring.datasource.url=jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable
```

## Testing

Run tests for this module:
```bash
# Run all tests
./mvnw test -pl datasource -DskipTests=false

# Run with specific profile
./mvnw test -pl datasource -DskipTests=false -Dspring.profiles.active=docker
```

## Best Practices

### Connection Management
- Always use try-with-resources to ensure connections are properly closed
- Leverage connection pooling (HikariCP is configured by default)
- Set appropriate pool sizes based on your workload

### Transaction Handling
- Explicitly manage transactions when needed
- Use `Connection.setAutoCommit(false)` for batch operations
- Always handle rollback scenarios properly

### Batch Operations
- Use `PreparedStatement.addBatch()` for bulk inserts/updates
- Set batch size to multiples of 128 for optimal CockroachDB performance
- Enable `reWriteBatchedInserts=true` in the JDBC URL

### Retry Logic
- Implement retry for serialization errors (SQL state 40001)
- Use Spring Retry with the custom `PostgresRetryClassifier`
- Consider exponential backoff for retry attempts

### Performance Tips
- Use prepared statements to avoid SQL parsing overhead
- Fetch only required columns in SELECT statements
- Consider using `Statement.setFetchSize()` for large result sets
- Monitor connection pool metrics for optimization

## Comparison with Other Approaches

| Aspect | DataSource | JDBCTemplate | JPA |
|--------|------------|--------------|-----|
| Control | Maximum | High | Medium |
| Complexity | High | Medium | Low |
| Boilerplate | Most | Some | Least |
| Performance | Best | Good | Variable |
| Learning Curve | Steep | Moderate | Gentle |

## Common Pitfalls

1. **Not closing resources**: Always use try-with-resources or ensure manual cleanup
2. **Ignoring batch size**: Not optimizing batch size for CockroachDB (use multiples of 128)
3. **Missing retry logic**: Not handling transient serialization failures
4. **Pool exhaustion**: Setting pool size too small for concurrent workload
5. **Transaction scope**: Not properly managing transaction boundaries

## Additional Resources

- [CockroachDB JDBC Documentation](https://www.cockroachlabs.com/docs/stable/build-a-java-app-with-cockroachdb.html)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Spring DataSource Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#jdbc-datasource)