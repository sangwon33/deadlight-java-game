@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass -File "%~dp0scripts\Run-Deadlight.ps1" %*
set "DEADLIGHT_EXIT_CODE=%errorlevel%"
if not "%DEADLIGHT_EXIT_CODE%"=="0" (
  echo.
  echo Deadlight stopped with exit code %DEADLIGHT_EXIT_CODE%.
  echo Keep this window open and share the error text above when asking for help.
  if not defined DEADLIGHT_NO_PAUSE pause
)
endlocal & exit /b %DEADLIGHT_EXIT_CODE%
