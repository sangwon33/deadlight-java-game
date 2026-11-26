param([ValidateRange(1,65535)][int]$Port=8080,[switch]$CheckTool)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
try {
    $tool=& (Join-Path $PSScriptRoot 'Get-Cloudflared.ps1')
    if($CheckTool){& $tool --version;exit $LASTEXITCODE}
    $health=Invoke-RestMethod -Uri ('http://127.0.0.1:'+$Port+'/api/health') -TimeoutSec 5
    if($health.status -ne 'ok' -or $health.service -ne 'deadlight-records'){throw 'This port is not running the Deadlight record server. Start SERVER.cmd from this project first.'}
    Write-Host 'Deadlight server is ready. Starting a temporary public HTTPS tunnel.'
    Write-Host 'Share the https://...trycloudflare.com address below with teammates.'
    Write-Host 'It is also saved to tunnel-address.txt. Keep this window and SERVER.cmd open.'
    Write-Host 'Press Ctrl+C to stop sharing. A new launch creates a new address.'
    $addressFile=Join-Path $projectRoot 'tunnel-address.txt'
    # Continue is scoped to native stderr forwarding; cloudflared logs on stderr on Windows.
    $ErrorActionPreference='Continue'
    & $tool tunnel --url ('http://127.0.0.1:'+$Port) --protocol http2 2>&1 | ForEach-Object {
        $line=$_.ToString();Write-Host $line
        if($line -match 'https://[a-z0-9-]+\.trycloudflare\.com') {
            [IO.File]::WriteAllText($addressFile,$Matches[0],[Text.UTF8Encoding]::new($false))
        }
    }
    exit $LASTEXITCODE
} catch {
    Write-Host ('Tunnel setup failed: '+$_.Exception.Message) -ForegroundColor Red
    Write-Host 'Start SERVER.cmd, wait for its startup message, then run TUNNEL.cmd again.'
    exit 1
}
