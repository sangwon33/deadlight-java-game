@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass -File "%~dp0scripts\Start-Tunnel.ps1" %*
set "DEADLIGHT_EXIT_CODE=%errorlevel%"
if not "%DEADLIGHT_EXIT_CODE%"=="0" if not defined DEADLIGHT_NO_PAUSE pause
endlocal & exit /b %DEADLIGHT_EXIT_CODE%
