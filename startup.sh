#!/bin/bash

# Stock Portfolio Application - Startup Script
# This script starts both the Spring Boot backend and React frontend

echo "🚀 Starting Stock Portfolio Application..."
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js v14 or higher."
    exit 1
fi

echo "✅ Java and Node.js are installed"
echo ""

# Start the Spring Boot backend
echo "📚 Starting Spring Boot Backend on http://localhost:8080..."
cd "$(dirname "$0")"
java -jar target/StockPotrfolio-0.0.1-SNAPSHOT.jar &
BACKEND_PID=$!

# Wait for backend to start
sleep 5

# Start the React frontend
echo "⚛️  Starting React Frontend on http://localhost:3000..."
cd src/main/resources/static
npm start &
FRONTEND_PID=$!

echo ""
echo "✅ Stock Portfolio Application is running!"
echo "📱 Frontend: http://localhost:3000"
echo "🔗 Backend: http://localhost:8080"
echo "📚 API Docs: http://localhost:8080/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Wait for both processes
wait $BACKEND_PID $FRONTEND_PID
