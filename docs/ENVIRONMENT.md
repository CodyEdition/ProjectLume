# Environment Configuration Guide

## Overview

Project Lume MVP supports configuration through environment variables and properties files. Environment variables take precedence over properties files.

## Configuration Methods

### Method 1: Environment Variables (Recommended for Production)

Set these environment variables before starting Tomcat:

```bash
export MYSQL_URL="jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC"
export MYSQL_USERNAME="projectlume_user"
export MYSQL_PASSWORD="your_secure_password"
export MYSQL_DRIVER="com.mysql.cj.jdbc.Driver"
```

### Method 2: Properties File (Development)

Edit `src/main/resources/database.properties`:

```properties
mysql.url=jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
mysql.username=projectlume_user
mysql.password=your_secure_password
mysql.driver=com.mysql.cj.jdbc.Driver
```

## Environment Variables

### Database Configuration

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `MYSQL_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC` | Yes |
| `MYSQL_USERNAME` | Database username | `projectlume_user` | Yes |
| `MYSQL_PASSWORD` | Database password | `secure_password` | Yes |
| `MYSQL_DRIVER` | JDBC driver class | `com.mysql.cj.jdbc.Driver` | Yes |

### JDBC URL Parameters

Common parameters for `MYSQL_URL`:

- `useSSL=false` - Disable SSL (use `true` in production with proper certificates)
- `serverTimezone=UTC` - Set server timezone
- `allowPublicKeyRetrieval=true` - Allow public key retrieval (development only)
- `dontTrackOpenResources=true` - Don't track open resources (prevents memory leaks)
- `useUnicode=true&characterEncoding=UTF-8` - Use UTF-8 encoding
- `autoReconnect=true` - Auto-reconnect on connection loss
- `maxReconnects=3` - Maximum reconnection attempts

**Production Example:**
```
jdbc:mysql://db.example.com:3306/projectlume?useSSL=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&maxReconnects=3
```

## Configuration Examples

### Development Environment

**Linux/macOS:**
```bash
# ~/.bashrc or ~/.zshrc
export MYSQL_URL="jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export MYSQL_USERNAME="root"
export MYSQL_PASSWORD="dev_password"
export MYSQL_DRIVER="com.mysql.cj.jdbc.Driver"
```

**Windows (PowerShell):**
```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="dev_password"
$env:MYSQL_DRIVER="com.mysql.cj.jdbc.Driver"
```

### Production Environment

**Tomcat setenv.sh (Linux/macOS):**
```bash
#!/bin/sh
# Database Configuration
export MYSQL_URL="jdbc:mysql://db-prod.example.com:3306/projectlume?useSSL=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8"
export MYSQL_USERNAME="projectlume_prod"
export MYSQL_PASSWORD="${DB_PASSWORD}"  # Load from secure vault
export MYSQL_DRIVER="com.mysql.cj.jdbc.Driver"

# JVM Options
export CATALINA_OPTS="-Xms1024m -Xmx2048m -XX:+UseG1GC"
```

**Tomcat setenv.bat (Windows):**
```batch
@echo off
set MYSQL_URL=jdbc:mysql://db-prod.example.com:3306/projectlume?useSSL=true&serverTimezone=UTC
set MYSQL_USERNAME=projectlume_prod
set MYSQL_PASSWORD=%DB_PASSWORD%
set MYSQL_DRIVER=com.mysql.cj.jdbc.Driver

set CATALINA_OPTS=-Xms1024m -Xmx2048m -XX:+UseG1GC
```

### Docker Environment

**docker-compose.yml:**
```yaml
services:
  app:
    image: tomcat:9.0
    environment:
      - MYSQL_URL=jdbc:mysql://db:3306/projectlume?useSSL=false&serverTimezone=UTC
      - MYSQL_USERNAME=projectlume_user
      - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      - MYSQL_DRIVER=com.mysql.cj.jdbc.Driver
    volumes:
      - ./target/project-lume-mvp.war:/usr/local/tomcat/webapps/ROOT.war
```

## Security Best Practices

### 1. Password Management
- **Never** commit passwords to version control
- Use environment variables or secure vaults in production
- Rotate passwords regularly
- Use strong, unique passwords

### 2. SSL/TLS Configuration
- Always use SSL (`useSSL=true`) in production
- Configure proper SSL certificates
- Use TLS 1.2 or higher

### 3. Connection Security
- Restrict database user permissions (grant only necessary privileges)
- Use firewall rules to restrict database access
- Use VPN or private networks for database connections
- Remove `allowPublicKeyRetrieval=true` in production

### 4. Environment Variable Security
- Store sensitive values in secure vaults (HashiCorp Vault, AWS Secrets Manager, etc.)
- Use separate credentials for development, staging, and production
- Limit access to environment configuration files
- Audit environment variable access

## Configuration Validation

The application validates configuration on startup. Check logs for:

```
INFO: Database connection established successfully
```

If you see errors, verify:
1. Environment variables are set correctly
2. Database is accessible from application server
3. Credentials are correct
4. Database exists and user has permissions

## Troubleshooting Configuration

### Issue: "Database configuration file not found"
**Solution:** Ensure `database.properties` exists in `WEB-INF/classes/` or set environment variables

### Issue: "Database connection failed"
**Solution:** 
1. Verify MySQL is running: `systemctl status mysql`
2. Test connection: `mysql -u $MYSQL_USERNAME -p -h hostname`
3. Check firewall rules
4. Verify JDBC URL format

### Issue: "Access denied for user"
**Solution:**
1. Verify username and password
2. Check user permissions: `SHOW GRANTS FOR 'user'@'host';`
3. Verify user can connect from application server host

### Issue: "Unknown database"
**Solution:**
1. Create database: `CREATE DATABASE projectlume;`
2. Verify database name in connection URL
3. Check database exists: `SHOW DATABASES;`

## Configuration Files

### database.properties
Location: `src/main/resources/database.properties`

This file is packaged into the WAR and can be overridden by environment variables.

### context.xml
Location: `src/main/webapp/META-INF/context.xml`

Tomcat context configuration. Can be overridden in Tomcat's conf directory.

### web.xml
Location: `src/main/webapp/WEB-INF/web.xml`

Web application deployment descriptor.

## Additional Resources

- [MySQL JDBC Connector Documentation](https://dev.mysql.com/doc/connector-j/8.0/en/)
- [Tomcat Environment Variables](https://tomcat.apache.org/tomcat-9.0-doc/config/context.html)
- See `DEPLOYMENT.md` for deployment-specific configuration

