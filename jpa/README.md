# JPA Example

## Overview

This example demonstrates Spring Data JPA with Hibernate for object-relational mapping to CockroachDB. JPA provides the highest level of abstraction, allowing you to work with Java objects rather than SQL, making it ideal for rapid development and domain-driven design.

## Key Features

- Entity-based data modeling with `@Entity` annotations
- Repository pattern with Spring Data JPA
- Automatic schema generation from entity definitions
- CRUD operations without writing SQL
- Batch operations with Hibernate batching
- Declarative transactions and retry logic
- Query methods derived from method names
- Support for JPQL and native queries

## Architecture

The `JpaApplication` launches two `ApplicationRunner` instances to demonstrate functionality:
- **JpaCRUDRunner** - Executes single item CRUD operations on the `User` entity
- **JpaBatchRunner** - Performs batch operations for bulk data processing

Database operations are performed by the `UserRepository` class which extends Spring's `CrudRepository`. Repository methods are not called directly by the application but by the `UserService` which enables transactional and retryable semantics via annotations.

At application startup, the database schema is automatically created by Hibernate based on the `User` entity definition. Verbose logging has been enabled on key packages to highlight the actions of various layers including the Postgres JDBC driver.

## Best Practices and Observations

### Set Isolation on DataSource
CockroachDB only supports "serializable" isolation.  While Spring's `@Transactioal` annotation allows you to override a database's default isolation level on a per transaction basis, this has no impact on CRDB.  Specifying a value for `isolation` on the annotation (` @Transactional(isolation = Isolation.SERIALIZABLE)`) will cause Spring to query and set the database isolation level before each transaction adding small but unnecessary overhead to each operation.  If you feel compelled to explicitly set the database isolation level in your application do it at the Datasource level instead... `spring.datasource.hikari.transaction-isolation=TRANSACTION_SERIALIZABLE`.

### Enable JPA Batching
JPA doesn't expose API level batching controls like straight JDBC or the `JdbcTemplate` helper.  To enable batch operations with JPA make sure the following properties are set:

```properties
spring.jpa.properties.hibernate.jdbc.batch_size=128
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

With CRDB and Java it is always good practice to set the batch size to a factor of `128`.  See https://www.cockroachlabs.com/docs/stable/build-a-java-app-with-cockroachdb.html#use-a-batch-size-of-128.

### Use `reWriteBatchedInserts=true`
When specifying the `spring.datasource.url` be sure to set the following parameter:  `reWriteBatchedInserts=true`.  This can significantly increase performance by collapsing multiple insert statements into a single, multi-row statement thus reducing statement overhead. See https://www.cockroachlabs.com/docs/stable/build-a-java-app-with-cockroachdb-hibernate.html#use-rewritebatchedinserts-for-increased-speed.

### Transactions and Retry
CRDB encourages the use of retry logic for database write operations (see https://www.cockroachlabs.com/docs/stable/transactions.html#transaction-retries).  The [Spring Retry](https://github.com/spring-projects/spring-retry) project makes implementing retry logic simple via two styles... declarative (annotation driven) or imperative (template driven).  Before implementing retry logic it is critically important to understand your specific implementation of transactions and how you want them to be retried.  Like many things, the options are endless.

* __Match styles and scope...__ if you are using declarative transaction management on a given method, use declarative retry logic at the method level too.  It can be confusing to match class and method level scoping.  Furthermore, mixing declarative and imperative styles is not only difficult to read but will likely lead to undesirable behavior.  For example, the `userRepository` call in this method will never be retried even tough it compiles and "looks" valid.

    ```java
    // Wrong :(
    @Transactional
    public User save(User user) {
        return retryTemplate.execute(context -> userRepository.save(user));
    }
    ``` 
  Retry logic is not called above because the declarative `@Transactional` annotation effectively wraps the method body in `BEGIN` and `COMMIT` statements.  Should a "retryable" exception be encountered it would likely happen during the `COMMIT` phase which is outside the scope of the method body that contains the imperative retry logic.  In other words, the code that encounters the "retryable" exception is not wrapped by the `retryTemplate`... whoops!  The following implementation behaves correctly:
  
  ```java
  // Right :)
  @Transactional
  @Retryable(exceptionExpression="@exceptionChecker.shouldRetry(#root)")
  public User save(User user) {
      return userRepository.save(user);
  }
  ```

## Performance Considerations

### JPA vs Other Approaches

| Aspect | JPA Performance | When to Use | When to Avoid |
|--------|----------------|-------------|---------------|
| Simple CRUD | Good | Domain-driven apps | High-throughput systems |
| Complex Queries | Variable | With JPQL/Criteria API | Multiple table joins |
| Batch Operations | Good with tuning | Bulk inserts/updates | Real-time processing |
| Memory Usage | Higher | Small-medium datasets | Large result sets |

### Optimization Tips

1. **Use lazy loading carefully** - Avoid N+1 query problems
2. **Enable second-level cache** - For frequently accessed reference data
3. **Tune batch size** - Set to 128 for CockroachDB optimization
4. **Use projection queries** - Fetch only required fields
5. **Monitor SQL generation** - Enable SQL logging in development

## Testing

Run tests for this module:
```bash
# Run all tests
./mvnw test -pl jpa -DskipTests=false

# Run specific test class
./mvnw test -Dtest=UserServiceTest -pl jpa -DskipTests=false
./mvnw test -Dtest=BusinessServiceTest -pl jpa -DskipTests=false

# Run retry-specific tests
./mvnw test -Dtest=UserServiceRetryTest -pl jpa -DskipTests=false
./mvnw test -Dtest=UserServiceLoopTest -pl jpa -DskipTests=false
```

## Common Pitfalls and Solutions

1. **LazyInitializationException**
   - Solution: Use `@Transactional` or fetch joins
   
2. **Poor batch performance**
   - Solution: Enable `batch_versioned_data=true` and set proper batch size
   
3. **Transaction retry failures**
   - Solution: Ensure retry logic wraps entire transaction
   
4. **Memory issues with large datasets**
   - Solution: Use pagination or streaming queries
   
5. **Slow startup times**
   - Solution: Consider `spring.jpa.hibernate.ddl-auto=validate` in production

## Additional Resources

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate with CockroachDB](https://www.cockroachlabs.com/docs/stable/build-a-java-app-with-cockroachdb-hibernate.html)
- [JPA Best Practices](https://www.baeldung.com/jpa-hibernate-best-practices)


