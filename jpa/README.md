# JPA Example
Simple example using Spring + JPA + Spring Retry.  The `JpaApplication` will launch two `ApplicationRunner` instances to demonstrate functionality... `JpaCRUDRunner` executes single item CRUD operations on the `User` entity, while `JpaBatchRunner` performs batch operations.

Database operations are performed by the `UserRepository` class which extends Spring's `CrudRepository`.  `UserRepository` methods are not called directly by the application but by the `UserService` which enables transactional and retryable semantics via annotations.

At application startup the database schema is automatically created by Hibernate based on the `User` entity definition.  Verbose logging has been enabled on key packages to highlight the actions of various layers including the Postgres JDBC driver.

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
CRDB encourages the use of retry logic for database write operations (see https://www.cockroachlabs.com/docs/stable/transactions.html#transaction-retries).  The Spring Retry project makes implementing retry logic simple via two styles... declarative (annotation driven) or imperative (template driven).  Before implementing retry logic it is critically important to understand your specific implementation of transactions and how you want them to be retried.  Like many things, the options are endless.

* __Match styles and scope...__ if you are using declarative transaction management on a given method, use declarative retry logic at the method level too.  It can be confusing to match class and method level scoping.  Furthermore, mixing declarative and imperative styles is not only difficult to read but will likely lead to undesirable behavior.  For example, the `userRepository` call in this method will never be retried even tough it compiles and "looks" valid.

    ```java
    @Transactional
    public User save(User user) {
        return retryTemplate.execute(context -> userRepository.save(user));
    }
    ``` 
  Retry logic is not called here because the declarative `@Transactional` annotation effectively wraps the method body in `BEGIN` and `COMMIT` statements.  Should a "retryable" exception be encountered it would likely happen during the `COMMIT` phase which is outside the scope of the method body that contains the imperative retry logic.  In other words, the code that encounters the "retryable" exception is not wrapped by the `retryTemplate`... whoops!
   


