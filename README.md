### Project Lume - MVP Second Iteration

Simple Java web application for the Project Lume MVP.

### Prerequisites
- JDK 21+ (LTS)
- Maven 3.9+
- MySQL (configure connection in `src/main/resources/database.properties`)

### Build
```bash
mvn clean package
```
The WAR will be produced under `target/`.

### Run / Deploy
- Deploy the generated WAR (`target/*.war`) to your servlet container (e.g. Tomcat/Jetty),

or
- Run the app with your preferred container locally.

### Database Initialization
The application includes a `com.projectlume.util.DatabaseInitializer` to apply schema setup. You can run it by launching your app or by executing the main class with your classpath. Ensure `database.properties` points to your MySQL instance.

### Useful Notes
- Default dev port used: 8081