package com.projectlume.verification;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test to verify WAR file contains all required components:
 * - Java source code in WEB-INF/sources/
 * - Compiled classes in WEB-INF/classes/
 * - Required dependencies in WEB-INF/lib/
 * - Web resources (JSP, CSS, fonts)
 * - Configuration files (web.xml, context.xml)
 * - Manifest entries for Tomcat 9 compatibility
 */
public class WarVerificationTest {
    
    private static final String WAR_FILE_NAME = "project-lume-mvp.war";
    private static final String TARGET_DIR = "target";
    private static JarFile warFile;
    
    // Required dependencies that must be present
    private static final String[] REQUIRED_DEPENDENCIES = {
        "mysql-connector-j",
        "jackson-databind",
        "jackson-core",
        "jackson-annotations",
        "jstl",
        "jbcrypt",
        "logback-classic",
        "logback-core",
        "slf4j-api"
    };
    
    // Required JSP files (sample check)
    private static final String[] REQUIRED_JSP_FILES = {
        "WEB-INF/views/login.jsp",
        "WEB-INF/views/dashboard.jsp",
        "WEB-INF/views/error.jsp"
    };
    
    // Required CSS files
    private static final String[] REQUIRED_CSS_FILES = {
        "assets/css/card-styles.css"
    };
    
    // Required font files
    private static final String[] REQUIRED_FONT_FILES = {
        "assets/fonts/Debata-Regular.otf",
        "assets/fonts/Debata-Italic.otf"
    };
    
    // Key classes that should be compiled
    private static final String[] REQUIRED_CLASSES = {
        "com/projectlume/servlet/AuthServlet.class",
        "com/projectlume/servlet/DashboardServlet.class",
        "com/projectlume/service/AuthService.class",
        "com/projectlume/model/User.class",
        "com/projectlume/model/Deck.class",
        "com/projectlume/model/Card.class"
    };
    
    // Key source files that should be included
    private static final String[] REQUIRED_SOURCE_FILES = {
        "com/projectlume/servlet/AuthServlet.java",
        "com/projectlume/servlet/DashboardServlet.java",
        "com/projectlume/service/AuthService.java",
        "com/projectlume/model/User.java",
        "com/projectlume/model/Deck.java",
        "com/projectlume/model/Card.java"
    };
    
    @BeforeAll
    public static void setUp() throws IOException {
        Path warPath = Paths.get(TARGET_DIR, WAR_FILE_NAME);
        assertTrue(Files.exists(warPath), 
            "WAR file not found at: " + warPath.toAbsolutePath() + 
            ". Run 'mvn package' first.");
        warFile = new JarFile(warPath.toFile());
    }
    
    @Test
    public void testWarFileExists() {
        Path warPath = Paths.get(TARGET_DIR, WAR_FILE_NAME);
        assertTrue(Files.exists(warPath), 
            "WAR file should exist at: " + warPath.toAbsolutePath());
    }
    
    @Test
    public void testSourceCodeIncluded() throws IOException {
        Set<String> sourceFiles = getEntriesMatching("WEB-INF/sources/.*\\.java$");
        
        assertFalse(sourceFiles.isEmpty(), 
            "WAR should contain Java source files in WEB-INF/sources/");
        
        // Verify minimum number of source files (at least 50 Java files expected)
        assertTrue(sourceFiles.size() >= 50, 
            "Expected at least 50 Java source files, found: " + sourceFiles.size());
        
        // Verify key source files are present
        for (String requiredSource : REQUIRED_SOURCE_FILES) {
            String fullPath = "WEB-INF/sources/" + requiredSource;
            assertTrue(sourceFiles.contains(fullPath) || 
                      sourceFiles.stream().anyMatch(s -> s.endsWith(requiredSource)),
                "Required source file not found: " + requiredSource);
        }
    }
    
    @Test
    public void testSourceStructureMatches() throws IOException {
        Set<String> sourceFiles = getEntriesMatching("WEB-INF/sources/.*\\.java$");
        
        // Verify package structure is maintained
        assertTrue(sourceFiles.stream().anyMatch(s -> s.contains("com/projectlume/servlet/")),
            "Servlet source files should be in com/projectlume/servlet/ package");
        assertTrue(sourceFiles.stream().anyMatch(s -> s.contains("com/projectlume/service/")),
            "Service source files should be in com/projectlume/service/ package");
        assertTrue(sourceFiles.stream().anyMatch(s -> s.contains("com/projectlume/model/")),
            "Model source files should be in com/projectlume/model/ package");
    }
    
    @Test
    public void testCompiledClassesIncluded() throws IOException {
        Set<String> classFiles = getEntriesMatching("WEB-INF/classes/.*\\.class$");
        
        assertFalse(classFiles.isEmpty(), 
            "WAR should contain compiled class files in WEB-INF/classes/");
        
        // Verify key classes are compiled
        for (String requiredClass : REQUIRED_CLASSES) {
            String fullPath = "WEB-INF/classes/" + requiredClass;
            assertTrue(classFiles.contains(fullPath) ||
                      classFiles.stream().anyMatch(s -> s.endsWith(requiredClass)),
                "Required compiled class not found: " + requiredClass);
        }
    }
    
    @Test
    public void testDependenciesIncluded() throws IOException {
        Set<String> libFiles = getEntriesMatching("WEB-INF/lib/.*\\.jar$");
        
        assertFalse(libFiles.isEmpty(), 
            "WAR should contain JAR dependencies in WEB-INF/lib/");
        
        // Verify all required dependencies are present
        Set<String> foundDependencies = new HashSet<>();
        for (String libFile : libFiles) {
            String fileName = libFile.substring(libFile.lastIndexOf('/') + 1);
            for (String requiredDep : REQUIRED_DEPENDENCIES) {
                if (fileName.contains(requiredDep)) {
                    foundDependencies.add(requiredDep);
                }
            }
        }
        
        for (String requiredDep : REQUIRED_DEPENDENCIES) {
            assertTrue(foundDependencies.contains(requiredDep),
                "Required dependency not found: " + requiredDep + 
                ". Found dependencies: " + foundDependencies);
        }
    }
    
    @Test
    public void testWebXmlExists() throws IOException {
        JarEntry webXml = warFile.getJarEntry("WEB-INF/web.xml");
        assertNotNull(webXml, "WEB-INF/web.xml should exist in WAR");
        assertFalse(webXml.isDirectory(), "WEB-INF/web.xml should be a file");
        
        // Verify it's not empty
        try (InputStream is = warFile.getInputStream(webXml)) {
            assertTrue(is.available() > 0, "WEB-INF/web.xml should not be empty");
        }
    }
    
    @Test
    public void testContextXmlExists() throws IOException {
        JarEntry contextXml = warFile.getJarEntry("META-INF/context.xml");
        assertNotNull(contextXml, "META-INF/context.xml should exist in WAR");
        assertFalse(contextXml.isDirectory(), "META-INF/context.xml should be a file");
        
        // Verify it's not empty
        try (InputStream is = warFile.getInputStream(contextXml)) {
            assertTrue(is.available() > 0, "META-INF/context.xml should not be empty");
        }
    }
    
    @Test
    public void testJspFilesIncluded() throws IOException {
        Set<String> jspFiles = getEntriesMatching("WEB-INF/views/.*\\.jsp$");
        
        assertFalse(jspFiles.isEmpty(), 
            "WAR should contain JSP files in WEB-INF/views/");
        
        // Verify required JSP files
        for (String requiredJsp : REQUIRED_JSP_FILES) {
            assertTrue(jspFiles.contains(requiredJsp) ||
                      jspFiles.stream().anyMatch(s -> s.endsWith(requiredJsp.substring(requiredJsp.lastIndexOf('/') + 1))),
                "Required JSP file not found: " + requiredJsp);
        }
    }
    
    @Test
    public void testCssFilesIncluded() throws IOException {
        Set<String> cssFiles = getEntriesMatching("assets/css/.*\\.css$");
        
        assertFalse(cssFiles.isEmpty(), 
            "WAR should contain CSS files in assets/css/");
        
        for (String requiredCss : REQUIRED_CSS_FILES) {
            assertTrue(cssFiles.contains(requiredCss) ||
                      cssFiles.stream().anyMatch(s -> s.endsWith(requiredCss.substring(requiredCss.lastIndexOf('/') + 1))),
                "Required CSS file not found: " + requiredCss);
        }
    }
    
    @Test
    public void testFontFilesIncluded() throws IOException {
        Set<String> fontFiles = getEntriesMatching("assets/fonts/.*\\.otf$");
        
        assertFalse(fontFiles.isEmpty(), 
            "WAR should contain font files in assets/fonts/");
        
        for (String requiredFont : REQUIRED_FONT_FILES) {
            assertTrue(fontFiles.contains(requiredFont) ||
                      fontFiles.stream().anyMatch(s -> s.endsWith(requiredFont.substring(requiredFont.lastIndexOf('/') + 1))),
                "Required font file not found: " + requiredFont);
        }
    }
    
    @Test
    public void testManifestExists() throws IOException {
        Manifest manifest = warFile.getManifest();
        assertNotNull(manifest, "WAR should contain MANIFEST.MF");
        
        // Verify required manifest entries
        assertNotNull(manifest.getMainAttributes().getValue("Implementation-Title"),
            "Manifest should contain Implementation-Title");
        assertNotNull(manifest.getMainAttributes().getValue("Implementation-Version"),
            "Manifest should contain Implementation-Version");
        assertNotNull(manifest.getMainAttributes().getValue("Build-Jdk"),
            "Manifest should contain Build-Jdk");
        assertNotNull(manifest.getMainAttributes().getValue("Created-By"),
            "Manifest should contain Created-By");
    }
    
    @Test
    public void testTomcat9Compatibility() throws IOException {
        Manifest manifest = warFile.getManifest();
        assertNotNull(manifest, "WAR should contain MANIFEST.MF");
        
        // Verify Servlet-Version entry for Tomcat 9 compatibility
        String servletVersion = manifest.getMainAttributes().getValue("Servlet-Version");
        assertNotNull(servletVersion, 
            "Manifest should contain Servlet-Version entry for Tomcat 9 compatibility");
        assertEquals("4.0", servletVersion,
            "Servlet-Version should be 4.0 for Tomcat 9 compatibility");
    }
    
    @Test
    public void testNoTestFilesIncluded() throws IOException {
        Set<String> testFiles = getEntriesMatching(".*test.*");
        
        // Verify no test source files are included
        long testSourceFiles = testFiles.stream()
            .filter(f -> f.contains("WEB-INF/sources/") && f.endsWith(".java"))
            .count();
        
        assertEquals(0, testSourceFiles,
            "WAR should not contain test source files. Found: " + testSourceFiles);
        
        // Verify no test class files are included
        long testClassFiles = testFiles.stream()
            .filter(f -> f.contains("WEB-INF/classes/") && f.endsWith(".class"))
            .count();
        
        assertEquals(0, testClassFiles,
            "WAR should not contain test class files. Found: " + testClassFiles);
    }
    
    @Test
    public void testDatabaseSchemaIncluded() throws IOException {
        JarEntry schema = warFile.getJarEntry("WEB-INF/classes/schema-mysql.sql");
        assertNotNull(schema, "Database schema file should be included in WAR");
        
        // Verify it's not empty
        try (InputStream is = warFile.getInputStream(schema)) {
            assertTrue(is.available() > 0, "schema-mysql.sql should not be empty");
        }
    }
    
    /**
     * Helper method to get all entries matching a regex pattern
     */
    private Set<String> getEntriesMatching(String pattern) {
        Pattern regex = Pattern.compile(pattern);
        Set<String> matches = new HashSet<>();
        
        Enumeration<JarEntry> entries = warFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (regex.matcher(name).matches()) {
                matches.add(name);
            }
        }
        
        return matches;
    }
}

