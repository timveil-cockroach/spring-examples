# CockroachDB + Spring Boot Examples

This repository is a collection of examples connecting to CockroachDB using the Spring Framework.  While there are multiple ways to access databases using the Spring Framework, this repository focuses on the most common patterns:

* **Datasource** - This example demonstrates how to connect to CockroachDB using the `jakarta.sql.DataSource` interface.  This approach is very powerful as it allows you to interact with JDBC primitives but is less common than the below patterns.  This is a good solution if you need to do something extra special that is not currently handled by higher level abstractions like `JDBCTemplate` 
* **JDBCTemplate** - JDBCTemplate is a powerful abstraction for native SQL access to CockroachDB.  This is a great solution when JPA/Hibernate is inappropriate or unavailable.  While easier to use than the raw DataSource, it still requires considerable boilerplate code.
* **JPA** - Perhaps the easiest approach to access data in CockroachDB, JPA allows developers to interact with the database using Java objects instead of native SQL.  This simplicity sometimes comes at the cost of performance but nonetheless is an extremely popular and powerful way to interact with CockroachDB.
* **Reactive** - In addition to the more traditional forms above, Spring has rich support for building "Reactive" or non-blocking, asynchronous applications.  This support includes Reactive database access.  This example shows how to connect CockroachDB using `r2dbc`.

In addition to basic Database access, the above examples include the use of `spring-retry` for declarative retry logic.  Retry logic is highly [recommended](https://www.cockroachlabs.com/docs/stable/transactions.html#client-side-intervention) when interacting with CockroachDB and the [Spring Retry](https://github.com/spring-projects/spring-retry) project makes implementing this logic incredibly simple.

## Prerequisites

* **Java 19+** - This project requires Java 19 or higher
* **Maven 3.6.3+** - For building the project (or use the included `mvnw` wrapper)
* **Docker Desktop** - For running local CockroachDB clusters (optional)
* **CockroachDB** - Either:
  * Local installation ([Download](https://www.cockroachlabs.com/docs/stable/install-cockroachdb.html))
  * Docker Compose examples in `docker/` directory
  * [CockroachCloud Serverless](https://cockroachlabs.cloud/) (free tier available)

## Quick Start

1. Clone the repository and build all modules:
```bash
git clone https://github.com/timveil/spring-examples.git
cd spring-examples
./mvnw clean package
```

2. Start a local CockroachDB cluster (easiest option):
```bash
cd docker/lb-haproxy
./up.sh  # On Mac/Linux
# or
up.cmd   # On Windows
```

3. Run an example application:
```bash
java -jar jpa/target/jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
```

## To Build
Currently, I do all my testing on an Intel based Mac.  I use Homebrew to install and keep all of my tooling up-to-date ([Maven](https://formulae.brew.sh/formula/maven#default), [JDK](https://formulae.brew.sh/cask/temurin), Docker Desktop, etc.).  To build simply clone the project and run `mvn clean package` from the root directory.  This will create 4 executable jars, one for each access pattern.  They can be found in each module's `target` directory.  For example:
* `datasource/target/datasource-20.0.0-SNAPSHOT.jar`
* `jdbc-template/target/jdbc-template-20.0.0-SNAPSHOT.jar`
* `jpa/target/jpa-20.0.0-SNAPSHOT.jar`
* `reactive/target/reactive-20.0.0-SNAPSHOT.jar`

# To Run
First things first you must have a working CockroachDB cluster to use these examples.  In the `docker` folder, I provide 2 examples, `lb-haproxy` and `lb-haproxy-secure`.  These examples use Docker Compose to locally launch 3 node clusters fronted by HAProxy in either a secure or insecure mode.  You can also download the cockroach binary and start a single node cluster or multiple nodes manually.  If you'd like a more scalable way to get started, I highly recommend signing up a forever free Serverless cluster.  

To help you get started, I've created a number of example `application.properties` files for each of the typical deployment methods.  These examples can be referenced by their corresponding Spring profile name.  The following profiles are currently supported:
* `docker` - configured to easily connect to the `lb-haproxy` example found in the `docker` folder.  This is the simplest configuration as it assumes a cluster running in `insecure` mode.
* `docker-secure` - configured to easily connect to the `lb-haproxy-secure` example found in the `docker` folder.  This configuration supports connecting to a `secure` cluster using `password` authentication
* `docker-secure-cert` - also, configured to easily connect to the `lb-haproxy-secure` example found in the `docker` folder.  Unlike the above example, this configuration supports connecting to a `secure` cluster using a client certificate instead of a password
* `serverless` - this profile provides a template for connecting to a CockroachCloud Serverless cluster which by default is secure.  While similar to `docker-secure-cert` there are a few additional parameters required for connecting to Serverless clusters

### Docker
Running examples using the `docker` profile is very straight forward.  Start the local docker cluster example found in `docker/lb-haproxy`.  Assuming the cluster starts successfully you can run any of the below commands.  Because this profile assumes a cluster is running on `localhost:26257`, this profile can also be used to connect to a single cockroach node started directly using the binary.

```
java -jar datasource-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
java -jar jdbc-template-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
java -jar jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
java -jar reactive-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
```

### Docker Secure
Running examples using the `docker-secure` profile requires a secure CockroachDB cluster. Start the local secure docker cluster example found in `docker/lb-haproxy-secure` by running the `./up.sh` script. This profile uses password authentication to connect to the secure cluster.

```
java -jar datasource-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure
java -jar jdbc-template-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure
java -jar jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure
java -jar reactive-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure
```

### Docker Secure Cert
Running examples using the `docker-secure-cert` profile also requires a secure CockroachDB cluster, but uses certificate-based authentication instead of password authentication. Start the local secure docker cluster example found in `docker/lb-haproxy-secure` by running the `./up.sh` script. Then, you need to copy the certificates from the Docker container to a local directory:

```bash
# Create a directory for certificates
mkdir -p certs

# Copy certificates from the Docker container
docker cp roach-cert:/.cockroach-certs/ca.crt ./certs/
docker cp roach-cert:/.cockroach-certs/client.root.crt ./certs/
docker cp roach-cert:/.cockroach-certs/client.root.key.pk8 ./certs/
```

Then run the examples with the `docker-secure-cert` profile, specifying the path to the certificates directory:

```
java -jar datasource-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure-cert --certs_dir=./certs
java -jar jdbc-template-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure-cert --certs_dir=./certs
java -jar jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure-cert --certs_dir=./certs
java -jar reactive-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure-cert --certs_dir=./certs
```

### Serverless
To run examples using the `serverless` profile, you need to have a CockroachCloud Serverless cluster. You can sign up for a free Serverless cluster at [CockroachCloud](https://cockroachlabs.cloud/).

Once you have your Serverless cluster:

1. Download the CA certificate for your cluster from the CockroachCloud console
2. Place the certificate in a directory (e.g., `./certs/serverless.crt`)
3. Run the examples with the `serverless` profile, providing your cluster details:

```
java -jar datasource-20.0.0-SNAPSHOT.jar --spring.profiles.active=serverless --certs_dir=./certs --cluster_name=your-cluster-name --username=your-username --password=your-password
java -jar jdbc-template-20.0.0-SNAPSHOT.jar --spring.profiles.active=serverless --certs_dir=./certs --cluster_name=your-cluster-name --username=your-username --password=your-password
java -jar jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=serverless --certs_dir=./certs --cluster_name=your-cluster-name --username=your-username --password=your-password
java -jar reactive-20.0.0-SNAPSHOT.jar --spring.profiles.active=serverless --certs_dir=./certs --cluster_name=your-cluster-name --username=your-username --password=your-password
```

Replace `your-cluster-name`, `your-username`, and `your-password` with your actual Serverless cluster details.

## Troubleshooting

### Common Issues

#### Connection Refused
If you get a connection refused error, ensure:
- The CockroachDB cluster is running (`docker ps` to check Docker containers)
- You're using the correct port (26257 for insecure, 26257 for secure)
- The correct Spring profile is active

#### Certificate Errors
For secure connections:
- Ensure certificates are in the correct directory
- Use absolute paths for certificate locations
- Check certificate permissions (should be readable)
- For `r2dbc` connections, certificates must use absolute paths (not classpath references)

#### Retry Logic Not Working
- Ensure `@EnableRetry` is present on your main application class
- Check that `@Retryable` annotations are on public methods
- Verify the exception is actually a retryable SQL state (40001, 40003, 08003, 08006)
- Review logs for `ExceptionChecker` output to see if exceptions are classified correctly

#### Build Failures
- Ensure you're using Java 19 or higher: `java -version`
- Clear Maven cache if needed: `./mvnw clean`
- For test failures, check database connectivity first

#### Docker Issues
- If `up.sh` fails on Mac/Linux, ensure it has execute permissions: `chmod +x up.sh`
- For "port already in use" errors, check for existing processes: `lsof -i :26257`
- Clean up Docker resources if needed: `docker system prune -a`

### Getting Help
- Check module-specific README files for detailed configuration
- Review the [CockroachDB documentation](https://www.cockroachlabs.com/docs/)
- See [Spring Boot documentation](https://spring.io/projects/spring-boot) for Spring-specific issues
