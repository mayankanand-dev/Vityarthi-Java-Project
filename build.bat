@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo        TradeX - Native Java Build System
echo ========================================================

if not exist bin mkdir bin
if not exist data mkdir data
if not exist exports mkdir exports

echo [1/3] Compiling source code with javac...
javac -encoding UTF-8 -d bin -cp "lib\sqlite-jdbc.jar;." src\tradex\app\*.java src\tradex\model\*.java src\tradex\model\enums\*.java src\tradex\repository\*.java src\tradex\database\*.java src\tradex\exchange\*.java src\tradex\service\*.java src\tradex\strategy\*.java src\tradex\simulation\*.java src\tradex\util\*.java src\tradex\exception\*.java src\tradex\test\*.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)
echo [OK] Compilation successful!

echo [2/3] Packaging into standalone tradex.jar...
cd bin
for %%j in (..\lib\*.jar) do (
    jar xf "%%j"
)
cd ..
jar cfe tradex.jar tradex.app.Main -C bin .

echo ========================================================
echo [SUCCESS] Build complete! Executable created: tradex.jar
echo Run application using: java -jar tradex.jar
echo Or simply execute:     run.bat
echo ========================================================
