@echo off
:: -----------------------------------------------------------------------------
:: Gradle start up script for Windows
:: -----------------------------------------------------------------------------

@REM Resolve the location of the script
setlocal
set DIR=%~dp0
set APP_HOME=%DIR%..\

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
java -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
