@echo off
setlocal

REM Personal Expense Manager - build Windows portable app (dist/)
REM Requirements:
REM - JDK 17+ (jpackage is bundled with JDK)
REM - Maven

set "APP_NAME=Personal Expense Manager"
set "APP_VERSION=1.0.0"
set "MAIN_CLASS=com.expensemanager.App"
set "MAIN_JAR=personal-expense-manager-1.0.0-SNAPSHOT.jar"

echo.
echo === Cleaning dist/ ===
if exist "dist" rmdir /s /q "dist"

echo.
echo === Checking tools ===
where mvn >nul 2>nul
if errorlevel 1 goto :NO_MVN

where jpackage >nul 2>nul
if errorlevel 1 goto :NO_JPACKAGE

echo.
echo === Maven package ===
call mvn -q clean package
if errorlevel 1 goto :MVN_FAIL

echo.
echo === Copy runtime dependencies ===
call mvn -q dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
if errorlevel 1 goto :DEPS_FAIL

echo.
echo === Preparing jpackage input ===
if exist "target\jpackage-input" rmdir /s /q "target\jpackage-input"
mkdir "target\jpackage-input" >nul 2>nul

if not exist "target\%MAIN_JAR%" goto :MISSING_MAIN_JAR

copy /y "target\%MAIN_JAR%" "target\jpackage-input\" >nul
copy /y "target\dependency\*.jar" "target\jpackage-input\" >nul

echo.
echo === jpackage (app-image) ===
jpackage ^
  --type app-image ^
  --name "%APP_NAME%" ^
  --app-version "%APP_VERSION%" ^
  --input "target/jpackage-input" ^
  --main-jar "%MAIN_JAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --dest "dist"

if errorlevel 1 goto :JPACKAGE_FAIL

echo.
echo [OK] Build completed.
echo Output: dist\
exit /b 0

:NO_MVN
echo [ERROR] Maven (mvn) not found in PATH.
exit /b 1

:NO_JPACKAGE
echo [ERROR] jpackage not found. Please install JDK 17+ and ensure it is in PATH.
exit /b 1

:MVN_FAIL
echo [ERROR] Maven build failed.
exit /b 1

:DEPS_FAIL
echo [ERROR] Failed to copy dependencies.
exit /b 1

:MISSING_MAIN_JAR
echo [ERROR] Main JAR not found: target\%MAIN_JAR%
echo         Check pom.xml artifactId/version, then update MAIN_JAR in this script.
exit /b 1

:JPACKAGE_FAIL
echo [ERROR] jpackage failed.
exit /b 1
