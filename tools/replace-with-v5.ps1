param(
    [Parameter(Mandatory=$true)]
    [string]$V5Source,

    [Parameter(Mandatory=$false)]
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\").Path
)

$ErrorActionPreference = 'Stop'

function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Ok($msg) { Write-Host "[ OK ] $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Write-Err($msg) { Write-Host "[ERR ] $msg" -ForegroundColor Red }

Write-Info "Project root: $ProjectRoot"
if (-not (Test-Path $ProjectRoot)) { Write-Err "Project root not found"; exit 1 }

# 1. Validate source path
if (-not (Test-Path $V5Source)) { Write-Err "V5 source not found: $V5Source"; exit 1 }
$srcIsZip = $false
if ((Get-Item $V5Source).Extension -in '.zip','.jar') { $srcIsZip = $true }

# 2. Backup current project
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupDir = Join-Path $ProjectRoot ("backup-$timestamp")
Write-Info "Creating backup at $backupDir"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

# Minimal backup: gradle files, build.gradle, settings.gradle, run/, and src/
$pathsToBackup = @(
    'build.gradle', 'settings.gradle', 'gradle.properties', 'gradlew', 'gradlew.bat',
    'gradle', 'src', 'run', 'README.md', 'README_NEW.md', 'LICENSE', 'plugin.yml', 'config.yml'
)
foreach ($rel in $pathsToBackup) {
    $abs = Join-Path $ProjectRoot $rel
    if (Test-Path $abs) {
        Write-Info "Backing up $rel"
        Copy-Item $abs -Destination $backupDir -Recurse -Force
    }
}
Write-Ok "Backup complete"

# 3. Remove existing source code
$srcDir = Join-Path $ProjectRoot 'src'
if (Test-Path $srcDir) {
    Write-Info "Removing existing src directory"
    Remove-Item $srcDir -Recurse -Force
}
New-Item -ItemType Directory -Path $srcDir -Force | Out-Null

# 4. Import v5 source
$tempExtract = Join-Path $ProjectRoot ("tmp-import-$timestamp")
New-Item -ItemType Directory -Force -Path $tempExtract | Out-Null

if ($srcIsZip) {
    Write-Info "Expanding archive: $V5Source"
    Expand-Archive -Path $V5Source -DestinationPath $tempExtract -Force
} else {
    Write-Info "Copying from directory: $V5Source"
    Copy-Item $V5Source\* $tempExtract -Recurse -Force
}

# Try to find nested src folder
$candidateSrc = @(Get-ChildItem -Path $tempExtract -Recurse -Directory -Filter 'src' | Select-Object -First 1)
if ($candidateSrc) {
    Write-Info "Found src at: $($candidateSrc.FullName)"
    Copy-Item $candidateSrc.FullName\* $srcDir -Recurse -Force
} else {
    Write-Warn "No src directory found; copying entire tree"
    Copy-Item $tempExtract\* $ProjectRoot -Recurse -Force
}

# 5. Restore/merge gradle files if present in v5
function Copy-IfExists($pathRel) {
    $cand = Join-Path $tempExtract $pathRel
    if (Test-Path $cand) {
        Write-Info "Updating $pathRel from v5 source"
        Copy-Item $cand (Join-Path $ProjectRoot $pathRel) -Recurse -Force
    }
}
Copy-IfExists 'build.gradle'
Copy-IfExists 'settings.gradle'
Copy-IfExists 'gradle.properties'
Copy-IfExists 'gradle'
Copy-IfExists 'plugin.yml'
Copy-IfExists 'config.yml'

# 6. Clean temp
Remove-Item $tempExtract -Recurse -Force
Write-Ok "Source replacement complete"

# 7. Build
Push-Location $ProjectRoot
try {
    Write-Info "Running Gradle build"
    ./gradlew clean build --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE" }
    Write-Ok "Build successful"

    $libs = Join-Path $ProjectRoot 'build\libs'
    $jar = Get-ChildItem $libs -Filter '*.jar' | Sort-Object Length -Descending | Select-Object -First 1
    if ($jar) {
        Write-Info ("Built JAR: {0} ({1:N0} bytes)" -f $jar.FullName, $jar.Length)
    } else {
        Write-Warn "No JAR found in build/libs"
    }
}
finally { Pop-Location }

Write-Ok "Done. Backups at: $backupDir"