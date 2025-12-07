# Project Lume MVP

A Java web application for flashcard study management, built with modern design patterns and optimized for Tomcat 9 deployment.

## Table of Contents

1. [Features](#features)
2. [Quick Start](#quick-start)
3. [System Requirements](#system-requirements)
4. [Building the Application](#building-the-application)
5. [Database Setup](#database-setup)
6. [Deployment to Tomcat 9](#deployment-to-tomcat-9)
7. [Configuration](#configuration)
8. [Project Structure](#project-structure)
9. [Design Patterns](#design-patterns)
10. [Troubleshooting](#troubleshooting)
11. [Technology Stack](#technology-stack)

## Features

- User authentication and registration
- Deck and card management
- Study sessions with progress tracking
- Study history and statistics
- Responsive web interface

## Quick Start

### Prerequisites

- **JDK 21+** (LTS recommended)
- **Maven 3.9+**
- **MySQL 8.0+** (or MariaDB 10.5+)
- **Apache Tomcat 9.0+** (for deployment)

### Development Build and Run

1. **Configure Database**
   ```bash
   # Edit src/main/resources/database.properties or set environment variables
   # See Configuration section below for details
   ```

2. **Build Application**
   ```bash
   mvn clean package
   ```

3. **Run with Embedded Tomcat**
   ```bash
   mvn cargo:run
   ```
   Application will be available at: http://localhost:8081/project-lume-mvp/

## System Requirements

### Minimum Requirements
- **Java**: JDK 21 or higher (LTS recommended)
- **Tomcat**: Apache Tomcat 9.0.x or higher
- **MySQL**: MySQL 8.0 or higher (or MariaDB 10.5+)
- **Operating System**: Linux, Windows Server, or macOS
- **Memory**: Minimum 2GB RAM (4GB+ recommended for production)
- **Disk Space**: 500MB for application + database storage

### Recommended Production Requirements
- **Java**: JDK 21 LTS
- **Tomcat**: Apache Tomcat 9.0.80+ (latest stable)
- **MySQL**: MySQL 8.0.35+ or MariaDB 10.11+
- **Memory**: 4GB+ RAM
- **CPU**: 2+ cores
- **Disk**: SSD with 10GB+ available space

## Building the Application

### Development Build (includes source files)
```bash
mvn clean package
```
The WAR file will be created at: `target/project-lume-mvp.war`

### Production Build (optimized, no source files)
```bash
mvn clean package -Pproduction
```
This creates an optimized WAR file without source code.

### Verify WAR Contents
```bash
# List WAR contents
jar -tf target/project-lume-mvp.war | head -20

# Check MANIFEST
jar -xf target/project-lume-mvp.war META-INF/MANIFEST.MF
cat META-INF/MANIFEST.MF
```

## Database Setup

### 1. Create Database and User

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE projectlume CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (replace 'password' with secure password)
CREATE USER 'projectlume_user'@'localhost' IDENTIFIED BY 'password';

-- Grant privileges
GRANT ALL PRIVILEGES ON projectlume.* TO 'projectlume_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Initialize Database Schema

The application automatically initializes the database schema on first startup via `DatabaseStartupListener`. Alternatively, you can manually run:

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.projectlume.util.DatabaseInitializer"

# Or directly
java -cp "target/project-lume-mvp/WEB-INF/classes:target/project-lume-mvp/WEB-INF/lib/*" \
     com.projectlume.util.DatabaseInitializer
```

## Deployment to Tomcat 9

### Prerequisites

1. **Install Java**
   ```bash
   # Verify Java installation
   java -version
   # Should show: openjdk version "21.x.x" or higher
   ```

2. **Install Apache Tomcat 9**
   - Download from: https://tomcat.apache.org/download-90.cgi
   
   **Linux/macOS:**
   ```bash
   # Extract Tomcat
   tar -xzf apache-tomcat-9.0.x.tar.gz
   sudo mv apache-tomcat-9.0.x /opt/tomcat9
   ```
   
   **Windows:**
   - Extract ZIP file to `C:\Program Files\Apache Software Foundation\Tomcat 9.0`

3. **Install MySQL**
   - Download and install MySQL 8.0+ from https://dev.mysql.com/downloads/mysql/
   - Create a database user and database for the application (see Database Setup above)

### Method 1: Manual Deployment (Recommended for Production)

#### Step 1: Stop Tomcat
```bash
# Linux/macOS
sudo systemctl stop tomcat9
# Or
/opt/tomcat9/bin/shutdown.sh

# Windows
# Stop Tomcat service from Services panel
# Or run: C:\tomcat9\bin\shutdown.bat
```

#### Step 2: Deploy WAR File
```bash
# Copy WAR file to Tomcat webapps directory
cp target/project-lume-mvp.war /opt/tomcat9/webapps/

# Linux/macOS - Set proper permissions
sudo chown tomcat:tomcat /opt/tomcat9/webapps/project-lume-mvp.war
sudo chmod 644 /opt/tomcat9/webapps/project-lume-mvp.war
```

#### Step 3: Configure Context Path (Optional)

To deploy at root context (`/` instead of `/project-lume-mvp`):

**Option A: Rename WAR file**
```bash
mv /opt/tomcat9/webapps/project-lume-mvp.war /opt/tomcat9/webapps/ROOT.war
```

**Option B: Create context.xml**
```bash
mkdir -p /opt/tomcat9/conf/Catalina/localhost
cat > /opt/tomcat9/conf/Catalina/localhost/project-lume-mvp.xml <<EOF
<Context docBase="/opt/tomcat9/webapps/project-lume-mvp" path="/" />
EOF
```

#### Step 4: Set Environment Variables

Create or edit `/opt/tomcat9/bin/setenv.sh` (Linux/macOS) or `setenv.bat` (Windows):

**Linux/macOS:**
```bash
#!/bin/sh
export MYSQL_URL="jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC"
export MYSQL_USERNAME="projectlume_user"
export MYSQL_PASSWORD="your_secure_password"
export MYSQL_DRIVER="com.mysql.cj.jdbc.Driver"

# JVM Options
export CATALINA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxMetaspaceSize=256m"
```

**Windows:**
```batch
set MYSQL_URL=jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC
set MYSQL_USERNAME=projectlume_user
set MYSQL_PASSWORD=your_secure_password
set MYSQL_DRIVER=com.mysql.cj.jdbc.Driver

set CATALINA_OPTS=-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxMetaspaceSize=256m
```

Make executable (Linux/macOS):
```bash
chmod +x /opt/tomcat9/bin/setenv.sh
```

#### Step 5: Start Tomcat
```bash
# Linux/macOS
sudo systemctl start tomcat9
# Or
/opt/tomcat9/bin/startup.sh

# Windows
# Start Tomcat service from Services panel
# Or run: C:\tomcat9\bin\startup.bat
```

#### Step 6: Verify Deployment
```bash
# Check Tomcat logs
tail -f /opt/tomcat9/logs/catalina.out

# Check application logs
tail -f /opt/tomcat9/logs/localhost.log

# Access application
curl http://localhost:8080/project-lume-mvp/
# Or open in browser: http://localhost:8080/project-lume-mvp/
```

### Method 2: Manager Application Deployment

1. Enable Manager application in `conf/tomcat-users.xml`:
```xml
<role rolename="manager-gui"/>
<role rolename="manager-script"/>
<user username="admin" password="secure_password" roles="manager-gui,manager-script"/>
```

2. Access Manager: http://localhost:8080/manager/html

3. Upload and deploy WAR file through the web interface

### Method 3: Hot Deployment (Development Only)

```bash
# Copy WAR to webapps directory while Tomcat is running
cp target/project-lume-mvp.war /opt/tomcat9/webapps/
# Tomcat will automatically detect and deploy
```

### Production Deployment Checklist

#### Pre-Deployment
- [ ] Build WAR file with production profile: `mvn clean package -Pproduction`
- [ ] Verify WAR file size and contents
- [ ] Test WAR file in staging environment
- [ ] Review and update database configuration
- [ ] Create database backup
- [ ] Document current production configuration

#### Deployment Steps
- [ ] Stop Tomcat service gracefully
- [ ] Backup existing deployment (if upgrading)
- [ ] Deploy new WAR file
- [ ] Set proper file permissions
- [ ] Configure environment variables
- [ ] Verify database connectivity
- [ ] Start Tomcat service
- [ ] Monitor startup logs for errors
- [ ] Verify application is accessible
- [ ] Test critical user flows
- [ ] Monitor application logs for 24 hours

#### Post-Deployment
- [ ] Verify all features are working
- [ ] Check application performance metrics
- [ ] Monitor error logs
- [ ] Verify database connections are stable
- [ ] Test user authentication and authorization
- [ ] Verify session management
- [ ] Check memory and CPU usage

### Performance Tuning

#### JVM Options (Recommended)

Add to `setenv.sh` or `setenv.bat`:

```bash
# Heap size (adjust based on available RAM)
-Xms1024m -Xmx2048m

# Garbage collector (G1GC recommended for Java 21)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200

# Metaspace
-XX:MaxMetaspaceSize=256m

# Logging
-Xlog:gc*:file=/opt/tomcat9/logs/gc.log:time,uptime:filecount=5,filesize=10M

# Additional optimizations
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/opt/tomcat9/logs/
-server
```

#### Tomcat Connector Configuration

Edit `conf/server.xml`:

```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"
           maxThreads="200"
           minSpareThreads="10"
           acceptCount="100"
           maxConnections="10000"
           compression="on"
           compressionMinSize="2048"
           compressableMimeType="text/html,text/xml,text/css,text/javascript,application/json"
           />
```

## Configuration

### Application Configuration

The application reads configuration from:
1. Environment variables (highest priority)
2. `database.properties` file in `WEB-INF/classes/`

### Environment Variables

The application supports configuration via environment variables:

- `MYSQL_URL` - JDBC connection URL
- `MYSQL_USERNAME` - Database username
- `MYSQL_PASSWORD` - Database password
- `MYSQL_DRIVER` - JDBC driver class

**Example:**
```bash
export MYSQL_URL="jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC"
export MYSQL_USERNAME="projectlume_user"
export MYSQL_PASSWORD="your_secure_password"
export MYSQL_DRIVER="com.mysql.cj.jdbc.Driver"
```

See [docs/ENVIRONMENT.md](docs/ENVIRONMENT.md) for complete configuration options.

### Database Properties File

Alternatively, configure via `src/main/resources/database.properties`:

```properties
mysql.url=jdbc:mysql://localhost:3306/projectlume?useSSL=false&serverTimezone=UTC
mysql.username=your_username
mysql.password=your_password
mysql.driver=com.mysql.cj.jdbc.Driver
```

### Context Configuration

The WAR includes `META-INF/context.xml` with optimized settings. For production, you may override these in:
- `$CATALINA_BASE/conf/[enginename]/[hostname]/[contextname].xml`

### Security Configuration

**Production Security Checklist:**
- [ ] Change default database passwords
- [ ] Use SSL/TLS for database connections in production
- [ ] Configure HTTPS in Tomcat (see Tomcat SSL documentation)
- [ ] Set secure session cookies (`secure=true` in production)
- [ ] Configure firewall rules
- [ ] Review and restrict Tomcat Manager access
- [ ] Set up proper file permissions

## Project Structure

```
project-lume-mvp/
├── src/
│   ├── main/
│   │   ├── java/com/projectlume/
│   │   │   ├── dao/          # Data Access Objects
│   │   │   ├── dto/          # Data Transfer Objects
│   │   │   ├── exception/    # Custom exceptions
│   │   │   ├── factory/      # Factory patterns
│   │   │   ├── model/        # Domain models
│   │   │   ├── repository/  # Repository interfaces
│   │   │   ├── service/      # Business logic services
│   │   │   ├── servlet/      # Web servlets
│   │   │   ├── strategy/     # Strategy pattern implementations
│   │   │   ├── command/       # Command pattern implementations
│   │   │   ├── builder/       # Builder pattern implementations
│   │   │   ├── facade/        # Facade pattern
│   │   │   ├── observer/      # Observer pattern
│   │   │   ├── decorator/     # Decorator pattern
│   │   │   ├── di/            # Dependency injection
│   │   │   └── util/          # Utility classes
│   │   ├── resources/         # Configuration files
│   │   └── webapp/            # Web resources (JSP, CSS, etc.)
│   └── test/                  # Test files
├── docs/                      # Documentation
└── pom.xml                    # Maven configuration
```

## Design Patterns

This application demonstrates exceptional use of design patterns:

- **Template Method** - BaseServlet and BaseService
- **Strategy** - Validation and study algorithms
- **Command** - Request handling
- **Builder** - DTO construction and query building
- **Facade** - ApplicationFacade for service simplification
- **Observer** - Event-driven architecture
- **Decorator** - Cross-cutting concerns (logging, caching)
- **Abstract Factory** - DAO factory with multiple implementations
- **Factory** - Service factory
- **Dependency Injection** - Service container and locator
- **Repository** - Generic repository pattern

## Troubleshooting

### Common Issues

#### Application Won't Start
1. Check Tomcat logs: `tail -f /opt/tomcat9/logs/catalina.out`
2. Verify Java version: `java -version` (must be 21+)
3. Check database connectivity
4. Verify WAR file is not corrupted
5. Check file permissions

#### Database Connection Errors
1. Verify MySQL is running: `systemctl status mysql`
2. Check database credentials in environment variables or properties file
3. Test connection: `mysql -u projectlume_user -p projectlume`
4. Check firewall rules
5. Verify database exists and user has permissions

#### Out of Memory Errors
1. Increase heap size in `setenv.sh`: `-Xmx2048m` or higher
2. Check for memory leaks in logs
3. Monitor memory usage: `jstat -gc <pid>`

#### 404 Errors
1. Verify context path: Check URL matches deployment path
2. Check `web.xml` configuration
3. Verify WAR file was extracted correctly
4. Check Tomcat logs for deployment errors

#### Session Issues
1. Check `web.xml` session configuration
2. Verify cookies are enabled in browser
3. Check session timeout settings
4. Verify session storage (file system vs memory)

### Log Files Location

- **Tomcat logs**: `/opt/tomcat9/logs/catalina.out`
- **Application logs**: `/opt/tomcat9/logs/localhost.log`
- **Access logs**: `/opt/tomcat9/logs/localhost_access_log.YYYY-MM-DD.txt`
- **GC logs**: `/opt/tomcat9/logs/gc.log` (if configured)

### Getting Help

1. Check application logs for specific error messages
2. Review Tomcat documentation: https://tomcat.apache.org/tomcat-9.0-doc/
3. Check MySQL error logs
4. Review troubleshooting guide: [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
5. Check environment configuration: [docs/ENVIRONMENT.md](docs/ENVIRONMENT.md)

## Technology Stack

- **Java**: 21
- **Web Framework**: Java Servlets, JSP
- **Database**: MySQL 8.0+
- **Build Tool**: Maven 3.9+
- **Application Server**: Apache Tomcat 9.0+
- **Libraries**: Jackson (JSON), Logback (Logging), BCrypt (Password Hashing), JSTL

## Additional Resources

- [Tomcat 9 Documentation](https://tomcat.apache.org/tomcat-9.0-doc/)
- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/refman/8.0/en/)
- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)

## Support

For deployment and configuration help:
- See this README for deployment instructions
- See [docs/ENVIRONMENT.md](docs/ENVIRONMENT.md) for configuration
- See [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for common issues

## License

[Add your license information here]
