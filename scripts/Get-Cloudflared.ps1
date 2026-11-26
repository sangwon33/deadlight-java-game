param()
$ErrorActionPreference='Stop'
$ProgressPreference='SilentlyContinue'
$projectRoot=Split-Path -Parent $PSScriptRoot
$toolDirectory=Join-Path $projectRoot 'tools'
$tool=Join-Path $toolDirectory 'cloudflared.exe'
$release=Get-Content -LiteralPath (Join-Path $toolDirectory 'CLOUDFLARED-INFO.json') -Raw | ConvertFrom-Json

function Get-ReleaseHash([string]$Path) {
    $hasher=[Security.Cryptography.SHA256]::Create()
    $stream=[IO.File]::OpenRead($Path)
    try { return [BitConverter]::ToString($hasher.ComputeHash($stream)).Replace('-','') }
    finally { $stream.Dispose();$hasher.Dispose() }
}

if(Test-Path -LiteralPath $tool) {
    if((Get-ReleaseHash $tool) -ne $release.Sha256) {
        throw 'tools/cloudflared.exe does not match the pinned release checksum. Restore the verified release before starting the tunnel.'
    }
    return $tool
}

# The binary is excluded from Git. A fresh checkout downloads the same pinned release.
$source=[Uri]$release.Source
if($source.Scheme -ne 'https' -or $source.Host -ne 'github.com' -or $source.AbsolutePath -notlike '/cloudflare/cloudflared/releases/download/*') {
    throw 'The cloudflared release source is not the official GitHub release URL.'
}
$download=Join-Path $toolDirectory ('cloudflared-'+[Guid]::NewGuid().ToString('N')+'.download')
try {
    Write-Host ('Downloading official cloudflared '+$release.Version+' (first run only)...')
    [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $source.AbsoluteUri -OutFile $download -UseBasicParsing -TimeoutSec 120
    if((Get-ReleaseHash $download) -ne $release.Sha256) { throw 'cloudflared download checksum verification failed.' }
    Move-Item -LiteralPath $download -Destination $tool
    return $tool
} catch {
    throw ('Could not prepare cloudflared: '+$_.Exception.Message+' Check the Internet connection, or download the pinned release in tools/CLOUDFLARED-INFO.json to tools/cloudflared.exe and retry.')
} finally {
    if(Test-Path -LiteralPath $download) { Remove-Item -LiteralPath $download }
}
