Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "       TradeX - Native Java Build System (PowerShell)   " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
if (-not (Test-Path "exports")) { New-Item -ItemType Directory -Path "exports" | Out-Null }
if (-not (Test-Path "data")) { New-Item -ItemType Directory -Path "data" | Out-Null }

Write-Host "[1/2] Compiling source code with javac..." -ForegroundColor Yellow
$javaSources = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d bin -cp "lib\sqlite-jdbc.jar;lib\*" $javaSources

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Compilation successful" -ForegroundColor Green

Write-Host "[2/2] Packaging into lightweight tradex.jar..." -ForegroundColor Yellow
$manifestContent = "Manifest-Version: 1.0`r`nMain-Class: tradex.app.Main`r`nClass-Path: lib/sqlite-jdbc.jar lib/slf4j-api.jar lib/slf4j-simple.jar`r`n"
Set-Content -Path "manifest.txt" -Value $manifestContent
jar cfm tradex.jar manifest.txt -C bin tradex
Remove-Item manifest.txt

Write-Host "========================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Build complete: tradex.jar" -ForegroundColor Green
Write-Host "Run application using: java -jar tradex.jar"
Write-Host "Or simply execute:     .\run.bat or .\run.ps1"
Write-Host "========================================================" -ForegroundColor Green
