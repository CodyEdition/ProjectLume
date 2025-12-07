# Troubleshooting Guide

## Quick Reference

| Issue | Quick Fix | See Section |
|-------|-----------|-------------|
| Application won't start | Check logs, verify Java 21+ | [Startup Issues](#startup-issues) |
| Database connection error | Verify MySQL is running, check credentials | [Database Issues](#database-issues) |
| Out of memory | Increase heap size in setenv.sh | [Memory Issues](#memory-issues) |
| 404 errors | Check context path, verify WAR deployment | [Deployment Issues](#deployment-issues) |
| Session problems | Check web.xml, verify cookies enabled | [Session Issues](#session-issues) |

## Startup Issues

### Application Fails to Start

**Symptoms:**
- Tomcat starts but application doesn't load
- 404 errors when accessing application
- Errors in catalina.out

**Diagnosis:**
```bash
# Check Tomcat logs
tail -f /opt/tomcat9/logs/catalina.out

# Check application-specific logs
tail -f /opt/tomcat9/logs/localhost.log

# Verify WAR file exists
ls -lh /opt/tomcat9/webapps/project-lume-mvp.war
```

**Common Causes and Solutions:**

1. **Java Version Mismatch**
   ```bash
   # Verify Java version
   java -version
   # Must be: openjdk version "21.x.x" or higher
   
   # Set JAVA_HOME if needed
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
   ```

2. **WAR File Not Deployed**
   ```bash
   # Check if WAR was extracted
   ls -la /opt/tomcat9/webapps/project-lume-mvp/
   
   # Redeploy WAR
   rm -rf /opt/tomcat9/webapps/project-lume-mvp*
   cp target/project-lume-mvp.war /opt/tomcat9/webapps/
   ```

3. **Missing Dependencies**
   ```bash
   # Check WEB-INF/lib directory
   ls /opt/tomcat9/webapps/project-lume-mvp/WEB-INF/lib/
   
   # Verify all JARs are present
   # Should include: mysql-connector-j, jackson-*, logback-*, etc.
   ```

4. **ClassNotFoundException**
   - Verify all dependencies are in WEB-INF/lib
   - Check for version conflicts
   - Rebuild WAR: `mvn clean package`

### Class Loading Errors

**Error:** `java.lang.ClassNotFoundException`

**Solution:**
1. Verify class exists in WAR: `jar -tf project-lume-mvp.war | grep ClassName`
2. Check package name matches directory structure
3. Rebuild application: `mvn clean compile package`

## Database Issues

### Connection Refused

**Error:** `Communications link failure` or `Connection refused`

**Diagnosis:**
```bash
# Test MySQL connectivity
mysql -u projectlume_user -p -h localhost projectlume

# Check MySQL status
systemctl status mysql

# Check MySQL port
netstat -tlnp | grep 3306
```

**Solutions:**
1. **MySQL Not Running**
   ```bash
   # Start MySQL
   sudo systemctl start mysql
   # Or
   sudo service mysql start
   ```

2. **Wrong Host/Port**
   - Verify MYSQL_URL: `jdbc:mysql://localhost:3306/projectlume`
   - Check MySQL is listening on correct port
   - Verify firewall allows connections

3. **Firewall Blocking**
   ```bash
   # Check firewall rules
   sudo ufw status
   sudo iptables -L -n | grep 3306
   
   # Allow MySQL port (if needed)
   sudo ufw allow 3306/tcp
   ```

### Access Denied

**Error:** `Access denied for user 'projectlume_user'@'localhost'`

**Solutions:**
1. **Verify Credentials**
   ```bash
   # Test login
   mysql -u projectlume_user -p
   ```

2. **Check User Permissions**
   ```sql
   -- Connect as root
   mysql -u root -p
   
   -- Check user exists
   SELECT User, Host FROM mysql.user WHERE User='projectlume_user';
   
   -- Check grants
   SHOW GRANTS FOR 'projectlume_user'@'localhost';
   
   -- Grant privileges if needed
   GRANT ALL PRIVILEGES ON projectlume.* TO 'projectlume_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Verify Database Exists**
   ```sql
   SHOW DATABASES LIKE 'projectlume';
   
   -- Create if missing
   CREATE DATABASE projectlume CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

### Database Schema Not Initialized

**Error:** `Table 'projectlume.users' doesn't exist`

**Solution:**
```bash
# Run database initializer
mvn exec:java -Dexec.mainClass="com.projectlume.util.DatabaseInitializer"

# Or manually run SQL
mysql -u projectlume_user -p projectlume < src/main/resources/schema-mysql.sql
```

## Memory Issues

### OutOfMemoryError

**Error:** `java.lang.OutOfMemoryError: Java heap space`

**Diagnosis:**
```bash
# Check current heap settings
ps aux | grep tomcat | grep Xmx

# Monitor memory usage
jstat -gc <tomcat_pid>
```

**Solutions:**
1. **Increase Heap Size**
   Edit `/opt/tomcat9/bin/setenv.sh`:
   ```bash
   export CATALINA_OPTS="-Xms1024m -Xmx2048m -XX:+UseG1GC"
   ```
   Restart Tomcat.

2. **Check for Memory Leaks**
   ```bash
   # Enable heap dump on OOM
   export CATALINA_OPTS="$CATALINA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/tomcat9/logs/"
   
   # Analyze heap dump with jvisualvm or Eclipse MAT
   ```

3. **Optimize Garbage Collection**
   ```bash
   export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
   ```

### High Memory Usage

**Symptoms:** Application becomes slow, frequent GC pauses

**Solutions:**
1. Monitor GC logs:
   ```bash
   export CATALINA_OPTS="$CATALINA_OPTS -Xlog:gc*:file=/opt/tomcat9/logs/gc.log:time,uptime"
   ```

2. Review connection pool settings
3. Check for unclosed database connections
4. Review application code for memory leaks

## Deployment Issues

### 404 Not Found

**Error:** `HTTP Status 404 - Not Found`

**Diagnosis:**
```bash
# Check context path
curl http://localhost:8080/project-lume-mvp/

# Verify WAR deployment
ls -la /opt/tomcat9/webapps/project-lume-mvp/

# Check Tomcat manager
curl http://localhost:8080/manager/text/list
```

**Solutions:**
1. **Wrong Context Path**
   - Access: `http://localhost:8080/project-lume-mvp/`
   - Or rename WAR to `ROOT.war` for root context

2. **WAR Not Extracted**
   ```bash
   # Check extraction
   ls /opt/tomcat9/webapps/project-lume-mvp/WEB-INF/
   
   # Force extraction
   cd /opt/tomcat9/webapps/
   unzip -o project-lume-mvp.war -d project-lume-mvp/
   ```

3. **web.xml Errors**
   - Check `WEB-INF/web.xml` syntax
   - Verify servlet mappings
   - Check for XML parsing errors in logs

### WAR File Issues

**Error:** `Failed to deploy application`

**Solutions:**
1. **Verify WAR File**
   ```bash
   # Check WAR structure
   jar -tf project-lume-mvp.war | head -20
   
   # Verify web.xml exists
   jar -xf project-lume-mvp.war WEB-INF/web.xml
   cat WEB-INF/web.xml
   ```

2. **Rebuild WAR**
   ```bash
   mvn clean package -Pproduction
   ```

3. **Check File Permissions**
   ```bash
   ls -l /opt/tomcat9/webapps/project-lume-mvp.war
   # Should be readable by tomcat user
   sudo chown tomcat:tomcat /opt/tomcat9/webapps/project-lume-mvp.war
   ```

## Session Issues

### Sessions Not Persisting

**Symptoms:** Users logged out unexpectedly, session data lost

**Solutions:**
1. **Check Session Configuration**
   - Verify `web.xml` session timeout
   - Check cookie settings

2. **Verify Cookies Enabled**
   - Browser must accept cookies
   - Check browser console for cookie errors

3. **Session Storage**
   - Default: Memory (lost on restart)
   - Consider persistent session storage for production

### Session Timeout Too Short

**Solution:** Edit `web.xml`:
```xml
<session-config>
    <session-timeout>60</session-timeout> <!-- minutes -->
</session-config>
```

## Performance Issues

### Slow Response Times

**Diagnosis:**
```bash
# Check Tomcat access logs
tail -f /opt/tomcat9/logs/localhost_access_log.*.txt

# Monitor database queries
mysql -u root -p -e "SHOW PROCESSLIST;"

# Check system resources
top
htop
```

**Solutions:**
1. **Database Optimization**
   - Add indexes to frequently queried columns
   - Optimize slow queries
   - Check connection pool size

2. **Tomcat Tuning**
   - Increase `maxThreads` in `server.xml`
   - Enable compression
   - Optimize JSP compilation

3. **Application Optimization**
   - Review slow servlet methods
   - Add caching where appropriate
   - Optimize database queries

### High CPU Usage

**Solutions:**
1. Profile application with JProfiler or VisualVM
2. Check for infinite loops
3. Review garbage collection frequency
4. Optimize database queries
5. Reduce JSP compilation overhead

## Log Analysis

### Finding Errors

```bash
# Search for errors in catalina.out
grep -i error /opt/tomcat9/logs/catalina.out

# Search for exceptions
grep -i exception /opt/tomcat9/logs/catalina.out

# Tail logs in real-time
tail -f /opt/tomcat9/logs/catalina.out | grep -i error
```

### Common Log Messages

**INFO:** Normal operation, can be ignored
**WARN:** Potential issues, investigate if problems occur
**ERROR:** Requires immediate attention
**FATAL:** Application cannot continue, requires immediate fix

## Getting Additional Help

1. **Check Logs First**
   - `catalina.out` - General Tomcat logs
   - `localhost.log` - Application-specific logs
   - `localhost_access_log.*.txt` - HTTP access logs

2. **Gather Information**
   - Java version: `java -version`
   - Tomcat version: Check `$CATALINA_HOME/RELEASE-NOTES`
   - MySQL version: `mysql --version`
   - Error messages from logs
   - Steps to reproduce

3. **Review Documentation**
   - `DEPLOYMENT.md` - Deployment guide
   - `ENVIRONMENT.md` - Configuration guide
   - `README.md` - Quick start

4. **Check External Resources**
   - [Tomcat 9 Documentation](https://tomcat.apache.org/tomcat-9.0-doc/)
   - [MySQL Documentation](https://dev.mysql.com/doc/)
   - [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)

