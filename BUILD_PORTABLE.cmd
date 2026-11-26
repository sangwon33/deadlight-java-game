@echo off
call "%~dp0RUN.cmd" -BuildPortable %*
exit /b %errorlevel%
