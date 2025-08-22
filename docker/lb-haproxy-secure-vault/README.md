# Secure CockroachDB Cluster with HAProxy and HashiCorp Vault

Advanced 3-node **secure** CockroachDB cluster with HAProxy load balancer and HashiCorp Vault for certificate management. This setup demonstrates production-like security practices with automated certificate rotation and centralized secrets management.

## Services

* `roach-0`, `roach-1`, `roach-2` - CockroachDB nodes in secure mode
* `lb` - HAProxy load balancer with SSL termination
* `roach-cert` - Dynamic certificate generator
* `roach-init` - Cluster initialization with user/database setup
* `vault` - HashiCorp Vault for secrets management
* `vault-client` - Vault client for certificate distribution

## Architecture

```
       HashiCorp Vault
             |
     (certificates)
             |
        Vault Agent
             |
    +--------+--------+
    |        |        |
    v        v        v
roach-0  roach-1  roach-2
    |        |        |
    +--------+--------+
             |
         HAProxy (lb)
             |
     Client Applications
```

## Getting Started

> If you are using Google Chrome as your browser, you may want to navigate here `chrome://flags/#allow-insecure-localhost` and set this flag to `Enabled`.

### Starting the Cluster

```bash
./up.sh
```

This script will:
1. Start all containers with Docker Compose
2. Initialize the CockroachDB cluster
3. Set up certificates via the roach-cert container
4. Configure HAProxy for load balancing
5. Create test user with password authentication

### Accessing Services

1. **CockroachDB UI**: https://localhost:8080
   - Username: `test`
   - Password: `password`
2. **HAProxy Stats**: http://localhost:8081
3. **SQL Connection**: `localhost:26257` (SSL required)

### Stopping the Cluster

```bash
./down.sh
```

### Cleaning Up

```bash
./prune.sh
```

## Vault Agent Configuration

The Vault Agent handles automatic certificate management through templates located in the `vault-agent/` directory:

### Certificate Templates

- `ca_cert.tmpl` - Root CA certificate
- `node_cert.tmpl` - Node certificates for inter-node communication
- `node_key.tmpl` - Node private keys
- `user_cert.tmpl` - Client certificates for user authentication
- `user_key.tmpl` - Client private keys
- `ui_cert.tmpl` - UI server certificates
- `ui_key.tmpl` - UI server private keys

### Agent Configuration (`agent.hcl`)

The Vault Agent configuration includes:
- Auto-authentication settings
- Certificate renewal policies
- Template rendering rules
- Output paths for certificates

## Connection Details

### Certificate-Based Authentication

```bash
# Copy certificates from roach-cert container
docker cp roach-cert:/.cockroach-certs/ca.crt ./certs/
docker cp roach-cert:/.cockroach-certs/client.root.crt ./certs/
docker cp roach-cert:/.cockroach-certs/client.root.key ./certs/
docker cp roach-cert:/.cockroach-certs/client.root.key.pk8 ./certs/

# Connect using cockroach SQL client
cockroach sql \
  --certs-dir=./certs \
  --host=localhost \
  --port=26257
```

### Password Authentication

```bash
# Connect with username and password (still requires CA cert)
cockroach sql \
  --url="postgresql://test:password@localhost:26257/defaultdb?sslmode=require&sslrootcert=./certs/ca.crt"
```

### Spring Application Configuration

For use with the Spring examples in this repository:

**Certificate Authentication:**
```bash
java -jar jpa/target/jpa-20.0.0-SNAPSHOT.jar \
  --spring.profiles.active=docker-secure-cert \
  --certs_dir=./certs
```

**Password Authentication:**
```bash
java -jar jpa/target/jpa-20.0.0-SNAPSHOT.jar \
  --spring.profiles.active=docker-secure
```

## Helpful Commands

### Interactive Shell Access

```bash
# CockroachDB nodes
docker compose exec roach-0 /bin/bash
docker compose exec roach-1 /bin/bash
docker compose exec roach-2 /bin/bash

# HAProxy
docker compose exec lb /bin/sh

# Certificate container
docker compose exec roach-cert /bin/sh
```

### Certificate Operations

```bash
# Copy all certificates at once
mkdir -p certs
docker cp roach-cert:/.cockroach-certs/ca.crt ./certs/
docker cp roach-cert:/.cockroach-certs/client.root.crt ./certs/
docker cp roach-cert:/.cockroach-certs/client.root.key ./certs/
docker cp roach-cert:/.cockroach-certs/client.root.key.pk8 ./certs/

# Verify certificate validity
openssl x509 -in certs/ca.crt -text -noout
openssl x509 -in certs/client.root.crt -text -noout

# Check certificate chain
openssl verify -CAfile certs/ca.crt certs/client.root.crt
```

### Cluster Administration

```bash
# Check cluster status
docker compose exec roach-0 /cockroach/cockroach node status --certs-dir=/certs

# Create a new user
docker compose exec roach-0 /cockroach/cockroach sql --certs-dir=/certs --execute="CREATE USER myuser WITH PASSWORD 'mypassword';"

# Grant privileges
docker compose exec roach-0 /cockroach/cockroach sql --certs-dir=/certs --execute="GRANT ALL ON DATABASE defaultdb TO myuser;"
```

## Security Features

### Certificate Management
- Dynamic certificate generation via `roach-cert` container
- Certificates mounted as read-only volumes
- Support for both node and client certificates
- Automatic certificate distribution to all nodes

### Network Security
- All inter-node communication encrypted with TLS
- Client connections require SSL/TLS
- HAProxy configured for SSL termination
- Internal network isolation via Docker networks

### Authentication Methods
- Certificate-based authentication (recommended)
- Password authentication with SSL
- User management via SQL commands

## Troubleshooting

### Certificate Issues

**Cannot connect - SSL error:**
```bash
# Ensure certificates are copied correctly
ls -la ./certs/

# Check certificate permissions
chmod 600 ./certs/client.root.key

# Verify CA certificate
openssl x509 -in ./certs/ca.crt -text -noout | grep "Subject:"
```

**Certificate not found:**
```bash
# Re-copy certificates from container
docker cp roach-cert:/.cockroach-certs/ ./certs/
```

### Connection Issues

**Connection refused:**
```bash
# Check if all services are running
docker compose ps

# Verify HAProxy is routing correctly
curl http://localhost:8081/stats

# Check CockroachDB logs
docker compose logs roach-0
```

**Authentication failed:**
```bash
# Verify user exists
docker compose exec roach-0 /cockroach/cockroach sql --certs-dir=/certs --execute="SELECT * FROM system.users;"

# Reset password if needed
docker compose exec roach-0 /cockroach/cockroach sql --certs-dir=/certs --execute="ALTER USER test WITH PASSWORD 'newpassword';"
```

### Cluster Issues

**Nodes not joining cluster:**
```bash
# Check node status
docker compose exec roach-0 /cockroach/cockroach node status --certs-dir=/certs

# Review logs for errors
docker compose logs roach-1 roach-2
```

## Advanced Configuration

### Custom Users and Databases

Create additional users and databases:
```sql
-- Connect as root
CREATE USER app_user WITH PASSWORD 'secure_password';
CREATE DATABASE app_db;
GRANT ALL ON DATABASE app_db TO app_user;
```

### HAProxy Configuration

The HAProxy configuration provides:
- Round-robin load balancing across all nodes
- Health checks every 2 seconds
- Automatic failover for failed nodes
- Statistics dashboard on port 8081

### Certificate Customization

To use custom certificates:
1. Replace certificates in the `roach-cert` container
2. Ensure proper file names and permissions
3. Restart the cluster

## Production Considerations

⚠️ **Important**: This configuration is for development and testing. For production:

1. **Use HashiCorp Vault** for dynamic certificate management
2. **Implement certificate rotation** policies
3. **Enable audit logging** for compliance
4. **Use dedicated networks** for node communication
5. **Implement backup strategies** for certificates and data
6. **Monitor certificate expiration** dates
7. **Use stronger passwords** and enforce password policies
8. **Implement RBAC** for fine-grained access control

## Comparison with Other Configurations

| Feature | lb-haproxy | lb-haproxy-secure | lb-haproxy-secure-vault |
|---------|------------|-------------------|-------------------------|
| Security | None | Basic SSL/TLS | Advanced with Vault |
| Certificates | N/A | Static | Dynamic with rotation |
| Authentication | None | Password/Cert | Password/Cert + Vault |
| Complexity | Low | Medium | High |
| Production Ready | No | Partial | Yes (with modifications) |

## Additional Resources

- [CockroachDB Security Documentation](https://www.cockroachlabs.com/docs/stable/security-overview.html)
- [HAProxy SSL/TLS Configuration](http://www.haproxy.org/)
- [HashiCorp Vault PKI Engine](https://www.vaultproject.io/docs/secrets/pki)
- [Docker Compose Networking](https://docs.docker.com/compose/networking/)