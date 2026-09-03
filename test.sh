#!/usr/bin/env bash
if [ ! -d "bin/tradex" ]; then
    echo "[INFO] Binaries not compiled. Running build.sh first..."
    ./build.sh
fi
echo "========================================================"
echo "       TradeX - Executing Automated Test Suite"
echo "========================================================"
java -cp "bin:lib/*" tradex.test.TestRunner
