Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "       TradeX - Native Java Build System (PowerShell)   " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
if (-not (Test-Path "exports")) { New-Item -ItemType Directory -Path "exports" | Out-Null }
if (-not (Test-Path "data")) { New-Item -ItemType Directory -Path "data" | Out-Null }

Write-Host "[1/3] Compiling source code with javac..." -ForegroundColor Yellow
$javaSources = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d bin -cp "lib\sqlite-jdbc.jar;lib\*" $javaSources

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Compilation successful" -ForegroundColor Green

Write-Host "[2/3] Packaging into standalone tradex.jar..." -ForegroundColor Yellow
Push-Location bin
jar xf "..\lib\sqlite-jdbc.jar"
jar xf "..\lib\slf4j-api.jar"
jar xf "..\lib\slf4j-simple.jar"
jar cfe "..\tradex.jar" tradex.app.Main tradex org META-INF
Pop-Location

Write-Host "========================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Build complete: tradex.jar" -ForegroundColor Green
Write-Host "Run application using: java -jar tradex.jar"
Write-Host "Or simply execute:     .\run.bat or .\run.ps1"
Write-Host "========================================================" -ForegroundColor Green
