#!/bin/bash
echo "=== Crow Eye Algorithm - Hackathon Demo ==="
echo "Compiling Java files..."
javac -cp "src/core:src/ethics:src/fractal" demo/Main.java src/core/*.java src/ethics/*.java src/fractal/*.java

echo ""
echo "Running simulation..."
java -cp ".:src/core:src/ethics:src/fractal:demo" Main

echo ""
echo "Simulation complete!"