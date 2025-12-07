#!/bin/bash

# WAR File Verification Script
# Verifies that the WAR file is ready for deployment

set -e

WAR_FILE="${1:-target/project-lume-mvp.war}"
VERBOSE="${2:-false}"

echo "=========================================="
echo "WAR File Verification Script"
echo "=========================================="
echo "WAR File: $WAR_FILE"
echo ""

# Check if WAR file exists
if [ ! -f "$WAR_FILE" ]; then
    echo "ERROR: WAR file not found: $WAR_FILE"
    echo "Build the WAR file first: mvn clean package"
    exit 1
fi

echo "✓ WAR file exists"

# Check WAR file size
WAR_SIZE=$(stat -f%z "$WAR_FILE" 2>/dev/null || stat -c%s "$WAR_FILE" 2>/dev/null)
echo "✓ WAR file size: $(numfmt --to=iec-i --suffix=B $WAR_SIZE 2>/dev/null || echo "${WAR_SIZE} bytes")"

# Extract WAR temporarily for inspection
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

echo ""
echo "Extracting WAR file for inspection..."
unzip -q "$WAR_FILE" -d "$TEMP_DIR"

# Check required directories
echo ""
echo "Checking WAR structure..."
REQUIRED_DIRS=("WEB-INF" "WEB-INF/classes" "WEB-INF/lib" "META-INF")
for dir in "${REQUIRED_DIRS[@]}"; do
    if [ -d "$TEMP_DIR/$dir" ]; then
        echo "✓ $dir/ exists"
    else
        echo "✗ ERROR: $dir/ missing"
        exit 1
    fi
done

# Check required files
echo ""
echo "Checking required files..."
REQUIRED_FILES=("WEB-INF/web.xml" "META-INF/MANIFEST.MF" "META-INF/context.xml")
for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$TEMP_DIR/$file" ]; then
        echo "✓ $file exists"
    else
        echo "✗ ERROR: $file missing"
        exit 1
    fi
done

# Check MANIFEST.MF
echo ""
echo "Checking MANIFEST.MF..."
if grep -q "Implementation-Version" "$TEMP_DIR/META-INF/MANIFEST.MF"; then
    echo "✓ MANIFEST.MF contains version information"
    if [ "$VERBOSE" = "true" ]; then
        echo "  MANIFEST contents:"
        cat "$TEMP_DIR/META-INF/MANIFEST.MF" | sed 's/^/    /'
    fi
else
    echo "⚠ WARNING: MANIFEST.MF missing version information"
fi

# Check for source files (should not be in production)
echo ""
echo "Checking for source files..."
if [ -d "$TEMP_DIR/WEB-INF/sources" ]; then
    SOURCE_COUNT=$(find "$TEMP_DIR/WEB-INF/sources" -name "*.java" 2>/dev/null | wc -l)
    if [ "$SOURCE_COUNT" -gt 0 ]; then
        echo "⚠ WARNING: WAR contains $SOURCE_COUNT Java source files"
        echo "  Consider building with production profile: mvn clean package -Pproduction"
    else
        echo "✓ No source files found (production-ready)"
    fi
else
    echo "✓ No source files found (production-ready)"
fi

# Check required JAR dependencies
echo ""
echo "Checking required dependencies..."
REQUIRED_JARS=(
    "mysql-connector-j"
    "jackson-databind"
    "jackson-core"
    "jackson-annotations"
    "logback-classic"
    "logback-core"
    "slf4j-api"
    "jstl"
    "jbcrypt"
)

MISSING_JARS=()
for jar in "${REQUIRED_JARS[@]}"; do
    if ls "$TEMP_DIR/WEB-INF/lib/${jar}"*.jar 1> /dev/null 2>&1; then
        echo "✓ $jar found"
    else
        echo "✗ ERROR: $jar missing"
        MISSING_JARS+=("$jar")
    fi
done

if [ ${#MISSING_JARS[@]} -gt 0 ]; then
    echo ""
    echo "ERROR: Missing required dependencies:"
    printf '  - %s\n' "${MISSING_JARS[@]}"
    exit 1
fi

# Check web.xml syntax
echo ""
echo "Validating web.xml..."
if command -v xmllint &> /dev/null; then
    if xmllint --noout "$TEMP_DIR/WEB-INF/web.xml" 2>/dev/null; then
        echo "✓ web.xml is valid XML"
    else
        echo "✗ ERROR: web.xml contains XML syntax errors"
        xmllint --noout "$TEMP_DIR/WEB-INF/web.xml"
        exit 1
    fi
else
    echo "⚠ xmllint not available, skipping XML validation"
fi

# Check context.xml syntax
echo ""
echo "Validating context.xml..."
if command -v xmllint &> /dev/null; then
    if xmllint --noout "$TEMP_DIR/META-INF/context.xml" 2>/dev/null; then
        echo "✓ context.xml is valid XML"
    else
        echo "✗ ERROR: context.xml contains XML syntax errors"
        xmllint --noout "$TEMP_DIR/META-INF/context.xml"
        exit 1
    fi
else
    echo "⚠ xmllint not available, skipping XML validation"
fi

# Count total JARs
JAR_COUNT=$(find "$TEMP_DIR/WEB-INF/lib" -name "*.jar" | wc -l)
echo ""
echo "Total JAR dependencies: $JAR_COUNT"

# Summary
echo ""
echo "=========================================="
echo "Verification Summary"
echo "=========================================="
echo "✓ WAR file structure is valid"
echo "✓ Required files present"
echo "✓ Required dependencies included"
if [ "$SOURCE_COUNT" -eq 0 ] 2>/dev/null; then
    echo "✓ Production-ready (no source files)"
else
    echo "⚠ Contains source files (use -Pproduction profile)"
fi
echo ""
echo "WAR file is ready for deployment!"
echo ""
echo "Next steps:"
echo "  1. Review DEPLOYMENT.md for deployment instructions"
echo "  2. Configure environment variables (see docs/ENVIRONMENT.md)"
echo "  3. Deploy to Tomcat 9"
echo ""

