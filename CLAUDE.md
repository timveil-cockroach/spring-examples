# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is a Spring Boot multi-module Maven project demonstrating various patterns for connecting to CockroachDB. The repository contains examples for four different data access patterns:
- **datasource** - Direct JDBC DataSource access
- **jdbc-template** - Spring JDBCTemplate for SQL access  
- **jpa** - JPA/Hibernate for object-relational mapping
- **reactive** - R2DBC for reactive, non-blocking database access
- **common** - Shared utilities for retry logic and exception handling

## Build and Test Commands

### Building the Project
```bash
# Build all modules (skips tests by default)
./mvnw clean package

# Build with tests
./mvnw clean package -DskipTests=false

# Build specific module
./mvnw clean package -pl jpa
```

### Running Tests

The project now has a comprehensive testing strategy with unit tests (*Test.java) and integration tests (*IT.java):

#### Unit Tests (Fast, No Database Required)
```bash
# Run all unit tests - for CI/GitHub Actions
./mvnw test -DskipTests=false

# Run unit tests for specific module
./mvnw test -pl jdbc-template -DskipTests=false

# Run specific unit test class
./mvnw test -Dtest=UserServiceTest -pl jpa -DskipTests=false

# Run specific unit test method
./mvnw test -Dtest=UserServiceTest#shouldFindAllUsers -pl jpa -DskipTests=false
```

#### Integration Tests (Require CockroachDB)
```bash
# Run all integration tests - requires running CockroachDB
./mvnw verify -Pintegration-tests

# Run integration tests for specific module
./mvnw verify -pl jdbc-template -Pintegration-tests

# Run specific integration test class
./mvnw verify -Dtest=UserServiceIT -pl jpa -Pintegration-tests
```

#### Combined Testing
```bash
# Run both unit and integration tests - for full local testing
./mvnw verify -Pfull-test-suite

# Run all tests with default profile (unit tests only, integration tests skipped)
./mvnw test -DskipTests=false
```

### Testing Strategy Overview

**Unit Tests (*Test.java)**: 72 tests across all modules
- Test business logic with mocks and stubs
- No external dependencies (database, network)
- Fast execution (< 5 seconds)
- Run in CI/GitHub Actions

**Integration Tests (*IT.java)**: 23 tests in jdbc-template and jpa modules  
- Test actual CockroachDB integration
- Require running CockroachDB cluster
- Comprehensive end-to-end validation
- Run locally and in staging environments

### Maven Profiles

#### Default Profile
- **Unit tests**: ✅ Runs (*Test.java files)
- **Integration tests**: ❌ Skips (*IT.java files)
- **Usage**: `./mvnw test -DskipTests=false`
- **Purpose**: Fast feedback for CI/GitHub Actions

#### Integration Tests Profile (`-Pintegration-tests`)
- **Unit tests**: ✅ Runs (*Test.java files)  
- **Integration tests**: ✅ Runs (*IT.java files)
- **Usage**: `./mvnw verify -Pintegration-tests`
- **Purpose**: Full testing with CockroachDB

#### Full Test Suite Profile (`-Pfull-test-suite`)
- **Unit tests**: ✅ Runs (*Test.java files)
- **Integration tests**: ✅ Runs (*IT.java files)
- **Usage**: `./mvnw verify -Pfull-test-suite`
- **Purpose**: Complete testing for releases

### Running Applications
Each module produces an executable JAR. Use Spring profiles to configure database connections:
```bash
# Insecure local cluster (docker/lb-haproxy)
java -jar jpa/target/jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker

# Secure local cluster with password auth (docker/lb-haproxy-secure)
java -jar jpa/target/jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure

# Secure local cluster with certificate auth
java -jar jpa/target/jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure-cert --certs_dir=./certs

# CockroachCloud Serverless
java -jar jpa/target/jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=serverless --certs_dir=./certs --cluster_name=your-cluster --username=user --password=pass
```

## Key Architecture Components

### Retry Logic Architecture
All modules implement automatic retry logic for CockroachDB's serialization errors:

1. **ExceptionChecker** (common/src/main/java/io/crdb/spring/common/ExceptionChecker.java) - Determines if SQL exceptions are retryable based on SQL state codes (40001, 40003, 08003, 08006)

2. **PostgresRetryClassifier** (common/src/main/java/io/crdb/spring/common/PostgresRetryClassifier.java) - Spring Retry classifier that uses ExceptionChecker to apply retry policies

3. **@Retryable Annotation** - Declarative retry on service methods with configurable backoff
   
4. **RetryTemplate** - Programmatic retry configured in each application's main class

### Database Configuration Profiles
Each module supports multiple Spring profiles for different deployment scenarios:
- `docker` - Insecure local cluster on localhost:26257
- `docker-secure` - Secure cluster with password authentication on localhost:26257
- `docker-secure-cert` - Secure cluster with certificate authentication on localhost:26257  
- `serverless` - CockroachCloud Serverless cluster with required certificates

Configuration files are in `src/main/resources/application-{profile}.properties` for each module.

### Module-Specific Patterns

**JPA Module**: Uses Spring Data JPA repositories with @Entity classes. Implements both CRUD operations (JpaCRUDRunner) and batch operations (JpaBatchRunner).

**JDBC Template Module**: Direct SQL execution with Spring's JdbcTemplate, includes parameterized queries and row mappers.

**Reactive Module**: R2DBC for non-blocking database access with Project Reactor's Mono/Flux types.

**DataSource Module**: Low-level JDBC access for cases requiring direct control over connections and transactions.

### Docker Test Environments
The `docker/` directory contains Docker Compose setups for local CockroachDB clusters:
- `lb-haproxy/` - 3-node insecure cluster with HAProxy load balancer
- `lb-haproxy-secure/` - 3-node secure cluster with certificates and HAProxy
- `lb-haproxy-secure-vault/` - Secure cluster with HashiCorp Vault integration for certificate management

## Comprehensive Test Coverage

The project includes both unit tests and integration tests following a clear separation strategy:

### Test Structure by Module

#### Unit Tests (*Test.java) - 72 Total Tests

**Common Module** (31 tests)
- `ExceptionCheckerTest` - Tests retry logic for CockroachDB SQL exceptions (40001, 40003, 08003, 08006)
- `PostgresRetryClassifierTest` - Tests Spring Retry classifier integration 
- `UserDTOBuilderTest` - Tests data generation utilities with Faker integration

**Datasource Module** (6 tests)
- `UserServiceTest` - Tests JDBC-based service with mocks for connection management, batch processing, and retry logic

**JDBC Template Module** (14 tests)
- `UserServiceUnitTest` - Tests JdbcTemplate operations with mocks for SQL execution and batch operations
- `JdbcTemplateApplicationTest` - Tests Spring application configuration and bean creation

**JPA Module** (16 tests)
- `UserServiceTest` - Tests JPA service operations with repository mocks
- `BusinessServiceTest` - Tests complex business logic with transaction handling
- `UserBuilderTest` - Tests data generation with Faker mocks
- `JpaApplicationTest` - Tests Spring application configuration and bean creation

**Reactive Module** (21 tests)
- `CustomerTest` - Tests R2DBC entity with immutable fields and validation
- `CustomerRepositoryTest` - Tests reactive repository interface with Project Reactor StepVerifier

#### Integration Tests (*IT.java) - 23 Total Tests

**JDBC Template Module** (7 tests)
- `UserServiceIT` - Tests actual JdbcTemplate operations with CockroachDB integration
- `UserServiceRetryIT` - Tests retry behavior with actual database failures and concurrent access

**JPA Module** (16 tests)
- `UserServiceIT` - Tests JPA repository operations with database integration
- `BusinessServiceIT` - Tests complex business service with actual transactions
- `UserServiceLoopIT` - Tests concurrent access patterns
- `UserServiceRetryIT` - Tests retry behavior with actual database failures

### Test Features

#### Unit Test Capabilities
- **Mock-based testing**: Uses Mockito for dependency isolation
- **Fast execution**: Complete suite runs in < 5 seconds
- **No external dependencies**: Tests run without database or network
- **Business logic validation**: Comprehensive coverage of service methods, edge cases, and error handling
- **Framework integration**: Tests Spring configuration, retry policies, and bean creation

#### Integration Test Capabilities  
- **Real database testing**: Uses actual CockroachDB for end-to-end validation
- **Transaction testing**: Validates ACID properties and retry logic
- **Concurrent access**: Tests serialization conflicts and retry behavior
- **Performance validation**: Tests batch operations and large datasets

### Testing Best Practices

**Naming Conventions:**
- Unit tests: `*Test.java` (e.g., `UserServiceTest.java`)
- Integration tests: `*IT.java` (e.g., `UserServiceIT.java`)

**Mock Strategy:**
- Unit tests mock all external dependencies (repositories, databases, services)
- Integration tests use real CockroachDB connections
- Test data generated with controlled mocks (Faker) for predictable results

**Assertion Patterns:**
- Descriptive test method names with `@DisplayName` annotations
- Comprehensive edge case coverage (null inputs, empty collections, error conditions)
- Verification of both return values and side effects (method calls, database state)