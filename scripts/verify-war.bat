@echo off
REM WAR File Verification Script for Windows
REM Verifies that the WAR file is ready for deployment

setlocal enabledelayedexpansion

set "WAR_FILE=%~1"
if "%WAR_FILE%"=="" set "WAR_FILE=target\project-lume-mvp.war"
set "VERBOSE=%~2"

echo ==========================================
echo WAR File Verification Script
echo ==========================================
echo WAR File: %WAR_FILE%
echo.

REM Check if WAR file exists
if not exist "%WAR_FILE%" (
    echo ERROR: WAR file not found: %WAR_FILE%
    echo Build the WAR file first: mvn clean package
    exit /b 1
)

echo [OK] WAR file exists

REM Check WAR file size
for %%A in ("%WAR_FILE%") do set "WAR_SIZE=%%~zA"
echo [OK] WAR file size: %WAR_SIZE% bytes

REM Extract WAR temporarily for inspection
set "TEMP_DIR=%TEMP%\war-verify-%RANDOM%"
mkdir "%TEMP_DIR%" 2>nul

echo.
echo Extracting WAR file for inspection...
powershell -Command "Expand-Archive -Path '%WAR_FILE%' -DestinationPath '%TEMP_DIR%' -Force" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Failed to extract WAR file
    rmdir /s /q "%TEMP_DIR%" 2>nul
    exit /b 1
)

REM Check required directories
echo.
echo Checking WAR structure...
if exist "%TEMP_DIR%\WEB-INF" (
    echo [OK] WEB-INF\ exists
) else (
    echo [ERROR] WEB-INF\ missing
    goto :cleanup_error
)

if exist "%TEMP_DIR%\WEB-INF\classes" (
    echo [OK] WEB-INF\classes\ exists
) else (
    echo [ERROR] WEB-INF\classes\ missing
    goto :cleanup_error
)

if exist "%TEMP_DIR%\WEB-INF\lib" (
    echo [OK] WEB-INF\lib\ exists
) else (
    echo [ERROR] WEB-INF\lib\ missing
    goto :cleanup_error
)

if exist "%TEMP_DIR%\META-INF" (
    echo [OK] META-INF\ exists
) else (
    echo [ERROR] META-INF\ missing
    goto :cleanup_error
)

REM Check required files
echo.
echo Checking required files...
if exist "%TEMP_DIR%\WEB-INF\web.xml" (
    echo [OK] WEB-INF\web.xml exists
) else (
    echo [ERROR] WEB-INF\web.xml missing
    goto :cleanup_error
)

if exist "%TEMP_DIR%\META-INF\MANIFEST.MF" (
    echo [OK] META-INF\MANIFEST.MF exists
) else (
    echo [ERROR] META-INF\MANIFEST.MF missing
    goto :cleanup_error
)

if exist "%TEMP_DIR%\META-INF\context.xml" (
    echo [OK] META-INF\context.xml exists
) else (
    echo [ERROR] META-INF\context.xml missing
    goto :cleanup_error
)

REM Check MANIFEST.MF
echo.
echo Checking MANIFEST.MF...
findstr /C:"Implementation-Version" "%TEMP_DIR%\META-INF\MANIFEST.MF" >nul 2>&1
if errorlevel 1 (
    echo [WARNING] MANIFEST.MF missing version information
) else (
    echo [OK] MANIFEST.MF contains version information
    if "%VERBOSE%"=="true" (
        echo   MANIFEST contents:
        type "%TEMP_DIR%\META-INF\MANIFEST.MF" | more
    )
)

REM Check for source files
echo.
echo Checking for source files...
set "SOURCE_COUNT=0"
if exist "%TEMP_DIR%\WEB-INF\sources" (
    for /r "%TEMP_DIR%\WEB-INF\sources" %%f in (*.java) do set /a SOURCE_COUNT+=1
    if !SOURCE_COUNT! gtr 0 (
        echo [WARNING] WAR contains !SOURCE_COUNT! Java source files
        echo   Consider building with production profile: mvn clean package -Pproduction
    ) else (
        echo [OK] No source files found (production-ready)
    )
) else (
    echo [OK] No source files found (production-ready)
)

REM Check required JAR dependencies
echo.
echo Checking required dependencies...
set "MISSING_COUNT=0"

set "JARS=mysql-connector-j jackson-databind jackson-core jackson-annotations logback-classic logback-core slf4j-api jstl jbcrypt"
for %%j in (%JARS%) do (
    dir /b "%TEMP_DIR%\WEB-INF\lib\%%j*.jar" >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] %%j missing
        set /a MISSING_COUNT+=1
    ) else (
        echo [OK] %%j found
    )
)

if %MISSING_COUNT% gtr 0 (
    echo.
    echo ERROR: Missing required dependencies
    goto :cleanup_error
)

REM Count total JARs
set "JAR_COUNT=0"
for /r "%TEMP_DIR%\WEB-INF\lib" %%f in (*.jar) do set /a JAR_COUNT+=1
echo.
echo Total JAR dependencies: %JAR_COUNT%

REM Summary
echo.
echo ==========================================
echo Verification Summary
echo ==========================================
echo [OK] WAR file structure is valid
echo [OK] Required files present
echo [OK] Required dependencies included
if %SOURCE_COUNT% equ 0 (
    echo [OK] Production-ready (no source files)
) else (
    echo [WARNING] Contains source files (use -Pproduction profile)
)
echo.
echo WAR file is ready for deployment!
echo.
echo Next steps:
echo   1. Review DEPLOYMENT.md for deployment instructions
echo   2. Configure environment variables (see docs\ENVIRONMENT.md)
echo   3. Deploy to Tomcat 9
echo.

goto :cleanup_success

:cleanup_error
rmdir /s /q "%TEMP_DIR%" 2>nul
exit /b 1

:cleanup_success
rmdir /s /q "%TEMP_DIR%" 2>nul
exit /b 0

