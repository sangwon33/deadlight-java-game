param(
    [ValidateSet('lwjgl3:run', 'lwjgl3:lanRun', 'server:run')]
    [string]$Task = 'lwjgl3:run',
    [switch]$CheckJava
)

# Shared by all Windows launchers. Changes apply only to this child process.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$candidates = New-Object 'System.Collections.Generic.List[string]'
$seen = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)

function Add-Candidate([string]$HomePath) {
    if ([string]::IsNullOrWhiteSpace($HomePath)) { return }
    $HomePath = [Environment]::ExpandEnvironmentVariables($HomePath.Trim().Trim('"'))
    if ($seen.Add($HomePath)) { $candidates.Add($HomePath) }
}

function Read-Jdk([string]$HomePath) {
    $javaPath = Join-Path $HomePath 'bin\java.exe'
    if (-not (Test-Path -LiteralPath $javaPath -PathType Leaf)) { return $null }
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = New-Object System.Diagnostics.ProcessStartInfo
    $process.StartInfo.FileName = $javaPath
    $process.StartInfo.Arguments = '-XshowSettings:properties -version'
    $process.StartInfo.UseShellExecute = $false
    $process.StartInfo.CreateNoWindow = $true
    $process.StartInfo.RedirectStandardOutput = $true
    $process.StartInfo.RedirectStandardError = $true
    try {
        if (-not $process.Start()) { return $null }
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        if (-not $process.WaitForExit(8000)) {
            $process.Kill()
            return $null
        }
        $details = $stdout.GetAwaiter().GetResult() + "`n" + $stderr.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) { return $null }
        $versionMatch = [regex]::Match($details, '(?m)^\s*java\.specification\.version\s*=\s*(\d+)')
        $homeMatch = [regex]::Match($details, '(?m)^\s*java\.home\s*=\s*(.+?)\s*$')
        $archMatch = [regex]::Match($details, '(?m)^\s*sun\.arch\.data\.model\s*=\s*64\s*$')
        if (-not $versionMatch.Success -or -not $archMatch.Success) { return $null }
        $major = [int]$versionMatch.Groups[1].Value
        if ($major -lt 17) { return $null }
        # Resolves PATH shims such as Oracle's javapath to their actual JDK.
        $actualHome = $HomePath
        if ($homeMatch.Success -and (Test-Path -LiteralPath (Join-Path $homeMatch.Groups[1].Value 'bin\javac.exe'))) {
            $actualHome = $homeMatch.Groups[1].Value
        }
        if (-not (Test-Path -LiteralPath (Join-Path $actualHome 'bin\javac.exe') -PathType Leaf)) { return $null }
        [pscustomobject]@{ Home = $actualHome; Major = $major; Preferred = ($major -eq 21) }
    } catch {
        return $null
    } finally {
        $process.Dispose()
    }
}

try {
    $selected = $null
    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        Add-Candidate $env:JAVA_HOME
        $selected = Read-Jdk $env:JAVA_HOME
    }
    if ($null -eq $selected) {
        foreach ($commandName in @('javac.exe', 'java.exe')) {
            foreach ($command in @(Get-Command $commandName -CommandType Application -All -ErrorAction SilentlyContinue)) {
                Add-Candidate (Split-Path -Parent (Split-Path -Parent $command.Source))
            }
        }
        $roots = @(
            (Join-Path $env:USERPROFILE '.jdks'),
            (Join-Path $env:USERPROFILE '.gradle\jdks')
        )
        foreach ($programRoot in @($env:ProgramFiles, ${env:ProgramFiles(x86)})) {
            if ([string]::IsNullOrWhiteSpace($programRoot)) { continue }
            foreach ($vendor in @('Java', 'Eclipse Adoptium', 'AdoptOpenJDK', 'Microsoft', 'Amazon Corretto', 'Zulu', 'BellSoft', 'Semeru', 'SapMachine')) {
                $roots += Join-Path $programRoot $vendor
            }
        }
        foreach ($root in $roots) {
            if (-not (Test-Path -LiteralPath $root -PathType Container)) { continue }
            Add-Candidate $root
            foreach ($folder in @(Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue)) {
                Add-Candidate $folder.FullName
            }
        }
        foreach ($registryRoot in @('HKLM:\SOFTWARE\JavaSoft', 'HKLM:\SOFTWARE\Eclipse Adoptium', 'HKLM:\SOFTWARE\AdoptOpenJDK', 'HKLM:\SOFTWARE\WOW6432Node\JavaSoft')) {
            if (-not (Test-Path -LiteralPath $registryRoot)) { continue }
            foreach ($key in @(Get-Item -LiteralPath $registryRoot) + @(Get-ChildItem -LiteralPath $registryRoot -Recurse -ErrorAction SilentlyContinue)) {
                $values = Get-ItemProperty -LiteralPath $key.PSPath -ErrorAction SilentlyContinue
                foreach ($property in @('JavaHome', 'Path', 'InstallationPath')) {
                    if ($null -ne $values -and $null -ne $values.$property) { Add-Candidate ([string]$values.$property) }
                }
            }
        }
        $valid = @(
            foreach ($candidate in $candidates) {
                $jdk = Read-Jdk $candidate
                if ($null -ne $jdk) { $jdk }
            }
        )
        $selected = $valid | Sort-Object -Property @{Expression='Preferred';Descending=$true}, @{Expression='Major';Descending=$true} | Select-Object -First 1
    }
    if ($null -eq $selected) {
        Write-Host 'A 64-bit JDK 17 or newer was not found.' -ForegroundColor Yellow
        Write-Host 'An Eclipse runtime alone may not include the Java compiler.'
        Write-Host 'Install Temurin JDK 21 (Windows x64 MSI): https://adoptium.net/temurin/releases/?version=21'
        Write-Host 'Enable Add to PATH and Set JAVA_HOME in its installer, or point JAVA_HOME at your JDK folder.'
        Write-Host 'JAVA_HOME must contain both bin\java.exe and bin\javac.exe.'
        exit 1
    }
    $env:JAVA_HOME = $selected.Home
    $env:PATH = (Join-Path $selected.Home 'bin') + ';' + $env:PATH
    Write-Host ('Using JDK {0}: {1}' -f $selected.Major, $selected.Home)
    if ($CheckJava) { exit 0 }
    Set-Location -LiteralPath $projectRoot
    & (Join-Path $projectRoot 'gradlew.bat') '-p' $projectRoot $Task '--console=plain' '--stacktrace'
    exit $LASTEXITCODE
} catch {
    Write-Host ('Launcher error: ' + $_.Exception.Message) -ForegroundColor Red
    exit 1
}
