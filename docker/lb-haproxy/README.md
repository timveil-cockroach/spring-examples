# Insecure CockroachDB Cluster with HAProxy

Simple 3-node **insecure** CockroachDB cluster with HAProxy acting as load balancer. This setup is ideal for local development and testing.

## Services

* `roach-0` - CockroachDB node (port 26257, 8080)
* `roach-1` - CockroachDB node (port 26258, 8081)
* `roach-2` - CockroachDB node (port 26259, 8082)
* `lb` - HAProxy load balancer (port 26257 for SQL, 8080 for UI, 8081 for stats)
* `roach-init` - Initializes the cluster and creates database/user

## Quick Start

### Starting the Cluster

**Mac/Linux:**
```bash
./up.sh
```

**Windows:**
```cmd
up.cmd
```

### Accessing the Cluster

1. **CockroachDB UI**: http://localhost:8080
2. **HAProxy Stats**: http://localhost:8081
3. **SQL Connection**: `localhost:26257` (through load balancer)

### Stopping the Cluster

**Mac/Linux:**
```bash
./down.sh
```

**Windows:**
```cmd
down.cmd
```

### Cleaning Up

To remove all containers and volumes:

**Mac/Linux:**
```bash
./prune.sh
```

**Windows:**
```cmd
prune.cmd
```

## Connection Details

### Default Connection Parameters
- **Host**: localhost
- **Port**: 26257
- **Database**: defaultdb
- **User**: root
- **Password**: (none - insecure mode)
- **SSL Mode**: disable

### JDBC URL
```
jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable
```

### Spring Application Properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable
spring.datasource.username=root
spring.datasource.password=
```

## Using with Spring Examples

This cluster configuration corresponds to the `docker` Spring profile. Run any example application with:

```bash
java -jar [module]/target/[module]-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
```

Examples:
```bash
java -jar datasource/target/datasource-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
java -jar jdbc-template/target/jdbc-template-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
java -jar jpa/target/jpa-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
java -jar reactive/target/reactive-20.0.0-SNAPSHOT.jar --spring.profiles.active=docker
```

## Interactive Shell Access

To access a CockroachDB node shell:
```bash
docker compose exec roach-0 /bin/bash
docker compose exec roach-1 /bin/bash
docker compose exec roach-2 /bin/bash
```

To access the SQL client:
```bash
docker compose exec roach-0 /cockroach/cockroach sql --insecure
```

To access HAProxy shell:
```bash
docker compose exec lb /bin/sh
```

## HAProxy Configuration

HAProxy is configured to:
- Load balance SQL connections across all three nodes (round-robin)
- Provide a single endpoint for the CockroachDB UI
- Monitor node health and automatically remove failed nodes
- Expose statistics on port 8081

### Load Balancing Strategy
- **Algorithm**: Round-robin
- **Health Check**: TCP check on port 26257
- **Check Interval**: 2 seconds
- **Failure Threshold**: 2 failed checks

## Architecture

```
Client Applications
        |
        v
   HAProxy (lb)
   Port: 26257
        |
   +----+----+
   |    |    |
   v    v    v
roach-0 roach-1 roach-2
```

## Troubleshooting

### Cluster Won't Start
- Check if ports are already in use: `lsof -i :26257`
- Ensure Docker is running: `docker ps`
- Check Docker Compose logs: `docker compose logs`

### Connection Refused
- Wait a few seconds for the cluster to fully initialize
- Check if HAProxy is running: `docker compose ps lb`
- Verify the cluster status in the UI at http://localhost:8080

### Performance Issues
- This is an insecure cluster meant for development only
- For production-like testing, use the secure cluster configurations
- Consider adjusting Docker resource limits if needed

## Notes

⚠️ **Warning**: This is an INSECURE cluster configuration intended for development and testing only. Never use this configuration in production.

- No authentication required
- No encryption for client connections
- No encryption for inter-node communication
- All ports are exposed to the host

For secure cluster setup, see the `lb-haproxy-secure` or `lb-haproxy-secure-vault` configurations.