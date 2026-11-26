param([Parameter(Mandatory=$true)][string]$JdkHome)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
try {
    $env:JAVA_HOME=$JdkHome
    $env:PATH=(Join-Path $JdkHome 'bin')+';'+$env:PATH
    $jlink=Join-Path $JdkHome 'bin\jlink.exe'
    if(-not (Test-Path -LiteralPath $jlink)){throw 'A full JDK with jlink.exe is required to build the runtime.'}
    $null=& (Join-Path $PSScriptRoot 'Get-Cloudflared.ps1')
    Set-Location -LiteralPath $root
    & (Join-Path $root 'gradlew.bat') 'lwjgl3:jar' 'server:installDist' '--console=plain' '--stacktrace'
    if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
    $package=Join-Path $root ('build\portable\Deadlight-Windows-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))
    if(Test-Path -LiteralPath $package){throw 'The output directory already exists.'}
    New-Item -ItemType Directory -Path $package | Out-Null
    & $jlink --module-path (Join-Path $JdkHome 'jmods') --add-modules java.se,jdk.unsupported,jdk.crypto.ec,jdk.crypto.mscapi,jdk.management,jdk.charsets --strip-debug --no-header-files --no-man-pages --compress=2 --output (Join-Path $package 'runtime')
    if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
    $gameJar=Get-ChildItem -LiteralPath (Join-Path $root 'lwjgl3\build\libs') -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    Copy-Item -LiteralPath $gameJar.FullName -Destination (Join-Path $package 'Deadlight.jar')
    New-Item -ItemType Directory -Path (Join-Path $package 'server'),(Join-Path $package 'scripts') | Out-Null
    Copy-Item -LiteralPath (Join-Path $root 'server\build\install\server\lib') -Destination (Join-Path $package 'server') -Recurse
    Copy-Item -LiteralPath (Join-Path $root 'tools') -Destination $package -Recurse
    Copy-Item -LiteralPath (Join-Path $root 'scripts\Start-Tunnel.ps1') -Destination (Join-Path $package 'scripts\Start-Tunnel.ps1')
    Copy-Item -LiteralPath (Join-Path $root 'scripts\Get-Cloudflared.ps1') -Destination (Join-Path $package 'scripts\Get-Cloudflared.ps1')
    Copy-Item -LiteralPath (Join-Path $root 'TUNNEL.cmd') -Destination (Join-Path $package 'TUNNEL.cmd')
    Copy-Item -LiteralPath (Join-Path $root 'docs\START_HERE.txt') -Destination (Join-Path $package 'START_HERE.txt')
    Copy-Item -LiteralPath (Join-Path $root 'docs\HOSTED_SERVER.md') -Destination (Join-Path $package 'HOSTED_SERVER.md')
    if(Test-Path -LiteralPath (Join-Path $JdkHome 'NOTICE')){Copy-Item -LiteralPath (Join-Path $JdkHome 'NOTICE') -Destination (Join-Path $package 'JAVA-NOTICE.txt')}
    $finish=@('set "DEADLIGHT_EXIT_CODE=%errorlevel%"','if not "%DEADLIGHT_EXIT_CODE%"=="0" (','  echo Deadlight stopped with exit code %DEADLIGHT_EXIT_CODE%.','  echo Keep the console error text when asking for help.','  if not defined DEADLIGHT_NO_PAUSE pause',')','endlocal & exit /b %DEADLIGHT_EXIT_CODE%')
    $start=@('@echo off','setlocal','cd /d "%~dp0"')
    $client='"%~dp0runtime\bin\java.exe" --enable-native-access=ALL-UNNAMED -cp "%~dp0Deadlight.jar" '
    [IO.File]::WriteAllLines((Join-Path $package 'PLAY.cmd'),$start+@($client+'com.example.pixelgame.lwjgl3.LanLauncher')+$finish,[Text.Encoding]::ASCII)
    [IO.File]::WriteAllLines((Join-Path $package 'PLAY_HOST.cmd'),$start+@($client+'com.example.pixelgame.lwjgl3.Lwjgl3Launcher')+$finish,[Text.Encoding]::ASCII)
    $server=@('set "SPRING_PROFILES_ACTIVE="','set "DB_URL="','set "DB_USER=sa"','set "DB_PASSWORD="','set "SERVER_ADDRESS=127.0.0.1"','if not defined PORT set "PORT=8080"','"%~dp0runtime\bin\java.exe" "-Ddeadlight.dataDir=%~dp0data" "-Dserver.port=%PORT%" -cp "%~dp0server\lib\*" com.example.pixelgame.server.RecordServer')
    [IO.File]::WriteAllLines((Join-Path $package 'SERVER.cmd'),$start+$server+$finish,[Text.Encoding]::ASCII)
    Compress-Archive -LiteralPath $package -DestinationPath ($package+'.zip') -CompressionLevel Optimal
    $hasher=[Security.Cryptography.SHA256]::Create()
    $stream=[IO.File]::OpenRead($package+'.zip')
    try {$checksum=[BitConverter]::ToString($hasher.ComputeHash($stream)).Replace('-','')}
    finally {$stream.Dispose();$hasher.Dispose()}
    $info=[pscustomobject]@{Folder=$package;Zip=($package+'.zip');Sha256=$checksum}
    $info | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'build\portable\LAST-PACKAGE.json') -Encoding utf8
    Write-Host ('Deadlight Windows ZIP: '+$info.Zip)
    exit 0
} catch {
    Write-Host ('Portable build failed: '+$_.Exception.Message) -ForegroundColor Red
    exit 1
}
