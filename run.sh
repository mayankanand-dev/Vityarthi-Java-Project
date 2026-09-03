#!/usr/bin/env bash
if [ ! -f "tradex.jar" ]; then
    echo "[INFO] tradex.jar not found. Running build.sh first..."
    ./build.sh
fi
java -jar tradex.jar "$@"
