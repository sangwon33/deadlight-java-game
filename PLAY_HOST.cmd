@echo off
call "%~dp0RUN.cmd" -Task lwjgl3:run %*
exit /b %errorlevel%
