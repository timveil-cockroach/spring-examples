# CockroachDB + Spring Boot Examples

This repository is a collection of examples connecting to CockroachDB using the Spring Framework.  While are multiple ways to access databases using the Spring Framework, this repository focuses on the most common patterns:

* **Datasource** - This example demonstrates how to connect to CockroachDB using the `javax.sql.DataSource` interface.  This approach is very powerful as it allows you to interact with JDBC primitives but is less common than the below patterns.  This is a good solution if you need to do something extra special that is not currently handled by higher level abstractions like `JDBCTemplate` 
* **JDBCTemplate** - JDBCTemplate is a powerful abstraction for native SQL access to CockroachDB.  This is a great solution when JPA/Hibernate is inappropriate or unavailable.  While easier to use than the raw DataSource, it still requires considerable boilerplate code.
* **JPA** - Perhaps the easiest approach to access data in CockroachDB, JPA allows developers to interact with the database using Java objects instead of native SQL.  This simplicity sometimes comes at the cost of performance but nonetheless is an extremely popular and powerful way to interact with CockroachDB.
* **Reactive** - In addition to the more traditional forms above, Spring has rich support for building "Reactive" or non-blocking, asynchronous applications.  This support includes Reactive database access.  This example shows how to connect CockroachDB using `r2dbc`.

In addition to basic Database access, the above examples include the use of `spring-retry` for declarative retry logic.  Retry logic highly [recommended](https://www.cockroachlabs.com/docs/v21.1/transactions.html#client-side-intervention) when interacting with CockroachDB and the [Spring Retry]45(https://github.com/spring-projects/spring-retry) project makes implementing this logic incredibly simple.

## To Build
Currently, I do all my testing on an Intel based Mac.  I use Homebrew to install and keep all of my tooling up-to-date ([Maven](https://formulae.brew.sh/formula/maven#default), [JDK](https://formulae.brew.sh/cask/temurin), Docker Desktop, etc.).  To build simply clone the project and run `mvn clean package` from the root directory.  This will create 4 executable jars, one for each access pattern.  They can be found in each module's `target` directory.  For example:
* datasource/target/datasource-20.0.0-SNAPSHOT.jar
* jdbc-template/target/jdbc-template-20.0.0-SNAPSHOT.jar
* jpa/target/jpa-20.0.0-SNAPSHOT.jar
* reactive/target/reactive-20.0.0-SNAPSHOT.jar

# To Run
First things first you must have a working CockroachDB cluster to use these examples.  In the `docker` folder, I provide 2 examples, `lb-haproxy` and `lb-haproxy-secure`.  These examples use Docker Compose to locally launch 3 node clusters fronted by HAProxy in either a secure or insecure mode.  You can also download the cockroach binary and start a single node cluster or multiple nodes manually.  If you'd like a more scalable way to get started, I highly recommend signing up a forever free Serverless cluster.  To help you get started, I've created a number of example `application.properties` files for each of the typical deployment methods.  These examples can be referenced by their corresponding Spring profile name.  The following profiles are currently supported:
* `docker` - configured to easily connect to the `lb-haproxy` example found in the `docker` folder.  This is the simplest configuration as it assumes a cluster running in `insecure` mode.
* `docker-secure` - configured to easily connect to the `lb-haproxy-secure` example found in the `docker` folder.  This configuration supports connecting to a `secure` cluster using `password` authentication
* `docker-secure-cert` - also, configured to easily connect to the `lb-haproxy-secure` example found in the `docker` folder.  Unlike the above example, this configuration supports connecting to a `secure` cluster using a client certificate instead of a password
* `serverless` - this profile provides a template for connecting to a CockroachCloud Serverless cluster which by default is secure.  While similar to `docker-secure-cert` there are a few additional parameters required for connecting to Serverless clusters

Once you've selected your deployment model you can fire up an example like so...
```
java -jar datasource-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker-secure
```
The above command launches the "Datasource" example using the `docker-secure` profile.

