# Reactive Example

## Overview

This example demonstrates reactive, non-blocking database access to CockroachDB using Spring WebFlux and R2DBC. Reactive programming enables building highly scalable applications that can handle many concurrent connections with minimal thread usage.

## Key Features

- Non-blocking I/O with R2DBC (Reactive Relational Database Connectivity)
- Reactive Streams API with Project Reactor (Mono/Flux)
- Backpressure handling for data flow control
- Functional programming style with reactive operators
- Event-driven architecture support
- Efficient resource utilization with minimal threads

## When to Use Reactive

Choose the reactive approach when:
- Building high-concurrency applications (thousands of concurrent users)
- Implementing real-time data streaming
- Creating event-driven microservices
- Dealing with slow I/O operations that shouldn't block threads
- Building reactive REST APIs or WebSocket endpoints
- Integrating with other reactive systems (Kafka, RabbitMQ)

## Usage Example

```java
@Repository
public class CustomerRepository {
    private final DatabaseClient client;
    
    public Mono<Customer> save(Customer customer) {
        return client.sql("INSERT INTO customers (id, name, email) VALUES (:id, :name, :email)")
            .bind("id", customer.getId())
            .bind("name", customer.getName())
            .bind("email", customer.getEmail())
            .fetch()
            .rowsUpdated()
            .then(Mono.just(customer));
    }
    
    public Flux<Customer> findAll() {
        return client.sql("SELECT * FROM customers")
            .map((row, metadata) -> Customer.builder()
                .id(row.get("id", UUID.class))
                .name(row.get("name", String.class))
                .email(row.get("email", String.class))
                .build())
            .all();
    }
    
    public Mono<Customer> findById(UUID id) {
        return client.sql("SELECT * FROM customers WHERE id = :id")
            .bind("id", id)
            .map((row, metadata) -> mapToCustomer(row))
            .one();
    }
}

@RestController
public class CustomerController {
    private final CustomerRepository repository;
    
    @GetMapping("/customers")
    public Flux<Customer> getAllCustomers() {
        return repository.findAll();
    }
    
    @PostMapping("/customers")
    public Mono<Customer> createCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }
}
```

## Configuration

Key application properties for R2DBC:

```properties
# R2DBC connection configuration
spring.r2dbc.url=r2dbc:postgresql://localhost:26257/defaultdb?sslmode=disable
spring.r2dbc.username=root
spring.r2dbc.password=

# Connection pool settings
spring.r2dbc.pool.initial-size=5
spring.r2dbc.pool.max-size=20
spring.r2dbc.pool.max-idle-time=30m

# For secure connections (absolute paths required)
spring.r2dbc.properties.sslMode=REQUIRE
spring.r2dbc.properties.sslRootCert=/absolute/path/to/ca.crt
spring.r2dbc.properties.sslCert=/absolute/path/to/client.crt
spring.r2dbc.properties.sslKey=/absolute/path/to/client.key
```

## Testing

Run tests for this module:
```bash
# Run all tests
./mvnw test -pl reactive -DskipTests=false

# Run with specific profile
./mvnw test -pl reactive -DskipTests=false -Dspring.profiles.active=docker
```

## Best Practices and Observations
### Certificate Path Requirements
- **Important**: R2DBC requires **absolute paths** for SSL certificates
- Classpath references (`classpath:ca.crt`) are not supported in older versions
- As of `r2dbc-postgresql 0.8.7.RELEASE` and newer, you can place certificates in `src/main/resources` and reference them by filename only
- For production, always use absolute paths to avoid deployment issues

### CockroachCloud Serverless Configuration
- The `options=--cluster=${cluster_name}` parameter doesn't work with free tier
- Instead, prepend the cluster name to the database name in the URL
- Example: `r2dbc:postgresql://free-tier.aws-us-west-2.cockroachlabs.cloud:26257/cluster-name.defaultdb`

## Reactive Programming Best Practices

### Subscription Management
- Always ensure proper subscription to reactive streams
- Use `subscribe()` for fire-and-forget operations
- Return `Mono` or `Flux` from service methods for composition

### Error Handling
```java
public Mono<Customer> saveWithRetry(Customer customer) {
    return save(customer)
        .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
            .filter(throwable -> isRetryable(throwable)))
        .onErrorMap(DatabaseException.class, e -> 
            new ServiceException("Failed to save customer", e));
}
```

### Transaction Management
```java
@Transactional
public Mono<Void> transferFunds(UUID fromId, UUID toId, BigDecimal amount) {
    return Mono.zip(
        debitAccount(fromId, amount),
        creditAccount(toId, amount)
    ).then();
}
```

### Backpressure Handling
```java
public Flux<Customer> streamCustomers() {
    return repository.findAll()
        .onBackpressureBuffer(1000)  // Buffer up to 1000 items
        .delayElements(Duration.ofMillis(10));  // Rate limiting
}
```

## Performance Considerations

| Aspect | Reactive | Traditional (JPA/JDBC) |
|--------|----------|------------------------|
| Thread Usage | Minimal | One per request |
| Memory Footprint | Lower | Higher |
| Latency | Similar | Similar |
| Throughput | Higher under load | Lower under load |
| Complexity | Higher | Lower |
| Debugging | Harder | Easier |

## Common Pitfalls

1. **Blocking Operations**: Never block in reactive pipelines
   ```java
   // Wrong
   Mono.just(data).map(d -> blockingOperation(d))
   
   // Right
   Mono.fromCallable(() -> blockingOperation(data))
       .subscribeOn(Schedulers.boundedElastic())
   ```

2. **Not Subscribing**: Reactive streams are lazy - nothing happens without subscription

3. **Memory Leaks**: Forgetting to dispose subscriptions

4. **Error Propagation**: Not handling errors properly in the reactive chain

5. **Transaction Boundaries**: Understanding reactive transaction scope

## Additional Resources

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [R2DBC Documentation](https://r2dbc.io/)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [Reactive Streams Specification](https://www.reactive-streams.org/)
- [CockroachDB with R2DBC](https://www.cockroachlabs.com/docs/stable/build-a-spring-app-with-cockroachdb-and-r2dbc.html)