@echo off
setlocal
cd /d "%~dp0"
set "SERVER_ADDRESS=0.0.0.0"
echo Starting the shared ranking server on port 8080.
echo Players must use this computer's Wi-Fi IPv4 address.
echo Find it with ipconfig. Allow Java on your private Wi-Fi network if Windows asks.
call "%~dp0SERVER.cmd"
set "DEADLIGHT_EXIT_CODE=%errorlevel%"
endlocal & exit /b %DEADLIGHT_EXIT_CODE%
