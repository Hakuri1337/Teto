@echo off
setlocal
chcp 65001 >nul
set "SCRIPT_DIR=%~dp0"
set "JAR=%SCRIPT_DIR%build\teto-injector.jar"
if not exist "%JAR%" (
    echo Injector JAR not found: "%JAR%"
    echo Run injector\src\compile.bat first, or use run-injector.bat in the deploy directory.
    exit /b 1
)
set "JAVA_CMD=java"
if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
"%JAVA_CMD%" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 --add-modules jdk.attach -jar "%JAR%" %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
