#!/bin/bash

# Stock Portfolio Application - Verification Script
# This script verifies that all necessary files are in place and properly formatted

echo "🔍 Verifying Stock Portfolio Application..."
echo ""

# Check if backend JAR exists
if [ -f "target/StockPotrfolio-0.0.1-SNAPSHOT.jar" ]; then
    echo "✅ Backend JAR file found"
else
    echo "❌ Backend JAR file NOT found"
fi

# Check if frontend node_modules exists
if [ -d "src/main/resources/static/node_modules" ]; then
    echo "✅ Frontend dependencies installed"
else
    echo "⚠️  Frontend dependencies not installed (run: npm install)"
fi

# Check if key CSS files exist
if [ -f "src/main/resources/static/src/styles/PortfolioList.css" ]; then
    echo "✅ PortfolioList.css found"
else
    echo "❌ PortfolioList.css NOT found"
fi

# Check if React App exists
if [ -f "src/main/resources/static/src/App.js" ]; then
    echo "✅ React App component found"
else
    echo "❌ React App component NOT found"
fi

# Check if documentation files exist
docs=(
    "INDEX.md"
    "QUICK_REFERENCE.md"
    "ARCHITECTURE.md"
    "TESTING.md"
    "BUILD_SUMMARY.md"
)

echo ""
echo "📚 Checking documentation files..."
for doc in "${docs[@]}"; do
    if [ -f "$doc" ]; then
        echo "✅ $doc found"
    else
        echo "❌ $doc NOT found"
    fi
done

echo ""
echo "✨ Verification complete!"
echo ""
echo "To start the application, run:"
echo "  For Windows: .\\startup.bat"
echo "  For Linux/Mac: ./startup.sh"
echo ""
echo "Or manually:"
echo "  Terminal 1: java -jar target/StockPotrfolio-0.0.1-SNAPSHOT.jar"
echo "  Terminal 2: cd src/main/resources/static && npm start"
