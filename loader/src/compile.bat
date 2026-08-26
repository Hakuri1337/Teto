@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
set "OUT_DIR=%SCRIPT_DIR%..\build"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
javac -encoding UTF-8 -d "%OUT_DIR%" "%SCRIPT_DIR%$.java"
if errorlevel 1 exit /b %errorlevel%
echo Loader class written to "%OUT_DIR%\$.class"
endlocal
