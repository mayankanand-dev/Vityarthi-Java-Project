@echo off
if not exist tradex.jar (
    echo [INFO] tradex.jar not found. Running build.bat first...
    call build.bat
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
)

java --enable-native-access=ALL-UNNAMED -jar tradex.jar %*
