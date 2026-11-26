@echo off
call "%~dp0RUN.cmd" -Task server:run %*
exit /b %errorlevel%
