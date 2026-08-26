@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
set "OUT_DIR=%SCRIPT_DIR%..\build"
if not exist "%OUT_DIR%\classes" mkdir "%OUT_DIR%\classes"
javac -encoding UTF-8 -d "%OUT_DIR%\classes" "%SCRIPT_DIR%Agent.java" "%SCRIPT_DIR%Injector.java"
if errorlevel 1 exit /b %errorlevel%
jar cfm "%OUT_DIR%\teto-injector.jar" "%SCRIPT_DIR%MANIFEST.MF" -C "%OUT_DIR%\classes" .
if errorlevel 1 exit /b %errorlevel%
echo Injector written to "%OUT_DIR%\teto-injector.jar"
endlocal
