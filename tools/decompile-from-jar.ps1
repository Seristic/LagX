param(
    [Parameter(Mandatory=$true)]
    [string]$PluginJar,

    [Parameter(Mandatory=$false)]
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\").Path,

    [Parameter(Mandatory=$false)]
    [string]$DecompilerJar = (Join-Path $PSScriptRoot 'cfr.jar')
)

$ErrorActionPreference = 'Stop'
function Info($m){ Write-Host "[INFO] $m" -ForegroundColor Cyan }
function Ok($m){ Write-Host "[ OK ] $m" -ForegroundColor Green }
function Warn($m){ Write-Host "[WARN] $m" -ForegroundColor Yellow }
function Err($m){ Write-Host "[ERR ] $m" -ForegroundColor Red }

if (-not (Test-Path $PluginJar)) { Err "Plugin JAR not found: $PluginJar"; exit 1 }
Info "Project root: $ProjectRoot"

# 1) Prepare folders
$srcDir = Join-Path $ProjectRoot 'src'
$javaOut = Join-Path $ProjectRoot 'src\main\java'
$resOut = Join-Path $ProjectRoot 'src\main\resources'
$work = Join-Path $ProjectRoot ('tmp-decompile-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Force -Path $work | Out-Null

if (Test-Path $srcDir) {
  Info "Removing existing src directory"
  Remove-Item $srcDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $javaOut | Out-Null
New-Item -ItemType Directory -Force -Path $resOut | Out-Null

# 2) Extract JAR to working dir
Info "Extracting JAR to $work"
Push-Location $work
try {
  jar -xf $PluginJar
} finally { Pop-Location }
Ok "Extraction complete"

# 3) Copy resources (non-class metadata) to resources
$resourceCandidates = @('plugin.yml','config.yml','bungee.yml','paper-plugin.yml')
foreach ($rc in $resourceCandidates) {
  $found = Join-Path $work $rc
  if (Test-Path $found) {
    Info "Copy resource: $rc"
    Copy-Item $found $resOut -Force
  }
}
# Also copy any non-class files from root or resource-like dirs
Get-ChildItem -Path $work -Recurse -File | Where-Object { $_.Extension -notin '.class','.MF' } | ForEach-Object {
  $rel = $_.FullName.Substring($work.Length).TrimStart('\\')
  # Heuristic: if under META-INF or classes, skip
  if ($rel -like 'META-INF*') { return }
  if ($_.Extension -eq '.yml' -or $_.Extension -eq '.yaml' -or $_.Extension -eq '.json' -or $_.Extension -eq '.txt') {
    $target = Join-Path $resOut $rel
    New-Item -ItemType Directory -Force -Path (Split-Path $target -Parent) | Out-Null
    Copy-Item $_.FullName $target -Force
  }
}

# 4) Ensure decompiler exists (CFR)
if (-not (Test-Path $DecompilerJar)) {
  Warn "Decompiler not found at $DecompilerJar. Attempting to download CFR..."
  try {
    $cfrUrl = 'https://www.benf.org/other/cfr/cfr-0.152.jar'
    Info "Downloading $cfrUrl"
    Invoke-WebRequest -Uri $cfrUrl -OutFile $DecompilerJar -UseBasicParsing
    Ok "Downloaded CFR to $DecompilerJar"
  } catch {
    Err "Failed to download CFR automatically. Place a decompiler JAR at: $DecompilerJar and re-run."
    exit 1
  }
}

# 5) Build extra classpath from run/ libraries to help CFR recover types
$cpJars = @()
$runDir = Join-Path $ProjectRoot 'run'
if (Test-Path $runDir) {
  $cpJars = Get-ChildItem -Path $runDir -Recurse -Include *.jar -File | Select-Object -ExpandProperty FullName
}
$extraCP = $null
if ($cpJars.Count -gt 0) {
  $extraCP = [string]::Join(';', $cpJars)
  Info "Using extraclasspath with $($cpJars.Count) jars"
}

# 6) Run decompiler with options to improve output quality
Info "Decompiling classes to $javaOut (CFR)"
$javaExe = 'java'
$argList = @('-jar', $DecompilerJar, $PluginJar, '--outputdir', $javaOut, '--caseinsensitivefs', 'true',
  '--sugarenums', 'true', '--sugarboxing', 'true', '--decodeenumswitch', 'true', '--recovertypeclash', 'true',
  '--recovertypeinfo', 'true', '--renameillegalidents', 'true', '--silentswitch', 'true')
if ($extraCP) { $argList += @('--extraclasspath', $extraCP) }
& $javaExe @argList
if ($LASTEXITCODE -ne 0) {
  Warn "CFR failed with exit code $LASTEXITCODE. Trying Vineflower..."
  $vine = Join-Path $PSScriptRoot 'vineflower.jar'
  if (-not (Test-Path $vine)) {
    try {
      $vfUrl = 'https://repo1.maven.org/maven2/org/vineflower/vineflower/1.10.1/vineflower-1.10.1.jar'
      Info "Downloading Vineflower: $vfUrl"
      Invoke-WebRequest -Uri $vfUrl -OutFile $vine -UseBasicParsing
    } catch {
      Warn "Failed to download Vineflower: $($_.Exception.Message)"
    }
  }
  if (Test-Path $vine) {
    # Vineflower: java -jar vineflower.jar <in.jar> <outdir>
    Info "Decompiling with Vineflower to $javaOut"
    & $javaExe -jar $vine $PluginJar $javaOut
    if ($LASTEXITCODE -ne 0) {
      Warn "Vineflower failed with exit code $LASTEXITCODE. Trying Procyon..."
    } else {
      Ok "Vineflower decompile complete"
    }
  }
  if ($LASTEXITCODE -ne 0) {
    $procyon = Join-Path $PSScriptRoot 'procyon.jar'
    if (-not (Test-Path $procyon)) {
      try {
        $pcUrl = 'https://repo1.maven.org/maven2/org/bitbucket/mstrobel/procyon-compilertools/0.6.0/procyon-compilertools-0.6.0.jar'
        Info "Downloading Procyon: $pcUrl"
        Invoke-WebRequest -Uri $pcUrl -OutFile $procyon -UseBasicParsing
      } catch {
        Err "Failed to download Procyon; cannot continue decompilation."
        exit 1
      }
    }
    # Procyon: java -jar procyon.jar -jar-in <in.jar> -o <outdir>
    Info "Decompiling with Procyon to $javaOut"
    & $javaExe -jar $procyon -jar-in $PluginJar -o $javaOut
    if ($LASTEXITCODE -ne 0) { Err "Procyon failed with exit code $LASTEXITCODE"; exit 1 }
    Ok "Procyon decompile complete"
  }
}
if ($proc.ExitCode -ne 0) { Err "Decompiler exited with code $($proc.ExitCode)"; exit 1 }
Ok "Decompile complete"

# 6) Build
Push-Location $ProjectRoot
try {
  Info "Running Gradle build"
  ./gradlew clean build --no-daemon
  if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE" }
  Ok "Build successful"
  $libs = Join-Path $ProjectRoot 'build\libs'
  $jar = Get-ChildItem $libs -Filter '*.jar' | Sort-Object Length -Descending | Select-Object -First 1
  if ($jar) { Info ("Built JAR: {0} ({1:N0} bytes)" -f $jar.FullName, $jar.Length) } else { Warn "No JAR found in build/libs" }
}
finally { Pop-Location }

# 7) Clean work dir
Remove-Item $work -Recurse -Force
Ok "Done"