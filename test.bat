@echo off
if not exist bin\tradex (
    echo [INFO] Binaries not compiled. Running build.bat first...
    call build.bat
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
)

echo ========================================================
echo        TradeX - Executing Automated Test Suite
echo ========================================================
java --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" tradex.test.TestRunner
