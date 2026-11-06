@echo off
setlocal
cd /d "%~dp0"
if exist "%JAVA_HOME%\bin\java.exe" goto launch
for /d %%J in ("%ProgramFiles%\Eclipse Adoptium\jdk-*") do if exist "%%~fJ\bin\java.exe" set "JAVA_HOME=%%~fJ"
if exist "%JAVA_HOME%\bin\java.exe" goto launch
for /d %%J in ("%USERPROFILE%\.gradle\jdks\*") do if exist "%%~fJ\bin\java.exe" set "JAVA_HOME=%%~fJ"
if exist "%JAVA_HOME%\bin\java.exe" goto launch
echo Java was not found. Set JAVA_HOME to your installed JDK folder.
pause
exit /b 1
:launch
call "%~dp0gradlew.bat" -p "%~dp0." lwjgl3:run
if errorlevel 1 pause
endlocal