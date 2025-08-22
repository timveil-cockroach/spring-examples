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
```bash
# Run all tests
./mvnw test -DskipTests=false

# Run tests for specific module
./mvnw test -pl jdbc-template -DskipTests=false

# Run tests for newly created test suites
./mvnw test -pl common,datasource,reactive -DskipTests=false

# Run specific test class
./mvnw test -Dtest=UserServiceTest -pl jpa -DskipTests=false

# Run specific test method
./mvnw test -Dtest=UserServiceTest#testSaveUser -pl jpa -DskipTests=false

# Run unit tests only (excludes integration tests)
./mvnw test -pl common,datasource,reactive -DskipTests=false
```

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

## Unit Test Coverage

The project includes comprehensive unit test suites for critical business logic and data access patterns:

### Test Structure by Module

**Common Module** (31 tests)
- `ExceptionCheckerTest` - Tests retry logic for CockroachDB SQL exceptions (40001, 40003, 08003, 08006)
- `PostgresRetryClassifierTest` - Tests Spring Retry classifier integration 
- `UserDTOBuilderTest` - Tests data generation utilities with Faker integration

**Datasource Module** (6 tests)
- `UserServiceTest` - Tests JDBC-based service with connection management, batch processing, and retry logic

**Reactive Module** (21 tests)
- `CustomerTest` - Tests R2DBC entity with immutable fields and validation
- `CustomerRepositoryTest` - Tests reactive repository interface with Project Reactor StepVerifier

**JDBC Template Module** - Pre-existing comprehensive integration tests
- `UserServiceTest` - Tests JdbcTemplate operations with database integration
- `UserServiceRetryTest` - Tests retry behavior with actual database failures

**JPA Module** - Pre-existing comprehensive integration tests  
- `UserServiceTest`, `BusinessServiceTest` - Tests JPA repository operations with database integration

### Unit Test Features

**Core Functionality Coverage:**
- Exception handling and retry logic for CockroachDB serialization failures
- SQL state validation and error classification
- Spring Retry integration with custom classifiers
- JDBC connection management and transaction handling
- Reactive data access with R2DBC

**Testing Patterns:**
- Mock-based unit testing with Mockito
- Reactive testing with StepVerifier (Project Reactor)
- Edge case validation (null values, empty collections, special characters)
- Generic type safety and proper dependency injection
- Comprehensive assertion coverage with descriptive test names

**Test Execution:**
```bash
# Run only unit tests (fast, no database required)
./mvnw test -pl common,datasource,reactive

# Run integration tests (requires database)  
./mvnw test -pl jdbc-template,jpa

# Run all tests
./mvnw test -DskipTests=false
```

The unit tests provide rapid feedback during development and validate core business logic without requiring database connectivity, while integration tests verify end-to-end functionality with actual CockroachDB clusters.