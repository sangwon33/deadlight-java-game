@echo off
call "%~dp0RUN.cmd" -Task lwjgl3:lanRun %*
exit /b %errorlevel%
