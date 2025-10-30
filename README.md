Stage 2 MVP — Quick Deploy Notes (Tomcat 9 + HSQLDB)

Overview
- This MVP targets Tomcat 9 and uses HSQLDB (file mode) so the WAR is self-contained.
- JDBC URL (example): jdbc:hsqldb:file:${catalina.base}/webapps/app/WEB-INF/data/appdb;hsqldb.lock_file=false
- Servlet API 4.0.1 is 'provided' scope in Maven.
- Dependencies bundled in WAR: hsqldb, jbcrypt (for BCrypt password hashing), JSTL (if used).

One-time Setup
1) Start Tomcat 9 locally.
2) Create DB files at runtime: ensure the app has permission to write to WEB-INF/data.
3) On first run, run /db/init (a servlet or one-off JDBC runner) OR import schema_stage2.sql into your chosen DB console (HSQLDB or MySQL).

Build (Maven)
- mvn clean package
- Output: target/<your-artifact>-<version>.war
- Rename to app.war (optional) and drop into TOMCAT_HOME/webapps

Recommended Web.xml Snippet
-------------------------------------------------
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

  <display-name>Stage2-MVP</display-name>

  <!-- JDBC settings -->
  <context-param>
    <param-name>JDBC_URL</param-name>
    <param-value>jdbc:hsqldb:file:${catalina.base}/webapps/stage2/WEB-INF/data/appdb;hsqldb.lock_file=false</param-value>
  </context-param>
  <context-param>
    <param-name>JDBC_USER</param-name>
    <param-value>sa</param-value>
  </context-param>
  <context-param>
    <param-name>JDBC_PASS</param-name>
    <param-value></param-value>
  </context-param>

  <!-- Auth filter protects app routes except /auth/* and static -->
  <filter>
    <filter-name>AuthFilter</filter-name>
    <filter-class>com.team.app.web.AuthFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>AuthFilter</filter-name>
    <url-pattern>/app/*</url-pattern>
  </filter-mapping>

  <!-- Servlets -->
  <servlet>
    <servlet-name>AuthServlet</servlet-name>
    <servlet-class>com.team.app.web.AuthServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>AuthServlet</servlet-name>
    <url-pattern>/auth/*</url-pattern>
  </servlet-mapping>

  <servlet>
    <servlet-name>DeckServlet</servlet-name>
    <servlet-class>com.team.app.web.DeckServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>DeckServlet</servlet-name>
    <url-pattern>/app/decks/*</url-pattern>
  </servlet-mapping>

  <servlet>
    <servlet-name>CardServlet</servlet-name>
    <servlet-class>com.team.app.web.CardServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>CardServlet</servlet-name>
    <url-pattern>/app/cards/*</url-pattern>
  </servlet-mapping>

  <welcome-file-list>
    <welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
</web-app>
-------------------------------------------------

Maven (pom.xml) Essentials
- Dependencies:
  * javax.servlet-api 4.0.1 (scope: provided)
  * org.hsqldb:hsqldb
  * org.mindrot:jbcrypt
  * jakarta.servlet.jsp.jstl (if using JSTL)

Smoke Test Script (after deploy)
1) Register at /auth/register (creates users row with BCrypt hash).
2) Login at /auth/login (establishes HttpSession).
3) Create deck at /app/decks/new → should insert into decks with owner_id = current user.
4) Add cards /app/cards/new?deckId=... → should insert with FK to deck.
5) List decks /app/decks → only user’s decks visible.
6) Logout /auth/logout → session invalidated.

Two‑Browser Demo (MP4)
- Show login in Chrome and Firefox (or Edge), create/view a deck, add one card, logout.
- Keep it under 2 minutes.

Credits & Report
- Include who implemented Auth vs Decks/Cards, challenges & mitigations, and PRD/MVP mapping by requirement ID (e.g., AUTH‑1, DECK‑1, CARD‑2).

Security
- Always hash passwords with BCrypt; never store plaintext.
- Validate session on /app/* via filter and check CSRF (basic token on POST optional for MVP).
