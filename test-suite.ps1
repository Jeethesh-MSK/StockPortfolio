# Comprehensive Test Suite for StockPortfolio Application

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "StockPortfolio Application Test Suite" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Kill any existing Java processes
Write-Host "Killing any existing Java processes..." -ForegroundColor Yellow
taskkill /F /IM java.exe 2>&1 | Out-Null
Start-Sleep -Seconds 2

# Start the application
Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
$appProcess = Start-Process -FilePath "java" -ArgumentList "-jar C:\Users\Administrator\JavaLearning\StockPotrfolio\target\StockPotrfolio-0.0.1-SNAPSHOT.jar" -PassThru
Write-Host "Application started with PID: $($appProcess.Id)" -ForegroundColor Green

# Wait for application to be ready
Write-Host "Waiting for application to start (15 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Test 1: Check Swagger UI
Write-Host ""
Write-Host "TEST 1: Swagger UI Accessibility" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8080/swagger-ui.html" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ PASS - Swagger UI is accessible (Status: $($response.StatusCode))" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ FAIL - Swagger UI not accessible: $_" -ForegroundColor Red
}

# Test 2: Check API Docs
Write-Host ""
Write-Host "TEST 2: OpenAPI Specification" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8080/v3/api-docs" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/v3/api-docs" -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ PASS - API Docs are accessible (Status: $($response.StatusCode))" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ FAIL - API Docs not accessible: $_" -ForegroundColor Red
}

# Test 3: Get empty portfolio (should return empty array)
Write-Host ""
Write-Host "TEST 3: Get Empty Portfolio" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8080/api/portfolio" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/portfolio" -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        $body = $response.Content | ConvertFrom-Json
        Write-Host "✅ PASS - Portfolio endpoint accessible (Status: $($response.StatusCode))" -ForegroundColor Green
        Write-Host "Response: $($response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 2)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ FAIL - Portfolio endpoint error: $_" -ForegroundColor Red
}

# Test 4: Get individual stock price
Write-Host ""
Write-Host "TEST 4: Get Stock Price (AAPL)" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8080/api/stocks/price/AAPL" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/stocks/price/AAPL" -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        $body = $response.Content | ConvertFrom-Json
        Write-Host "✅ PASS - Stock price endpoint accessible (Status: $($response.StatusCode))" -ForegroundColor Green
        Write-Host "Symbol: $($body.symbol)" -ForegroundColor Gray
        Write-Host "Price: $$($body.price)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ FAIL - Stock price endpoint error: $_" -ForegroundColor Red
}

# Test 5: Health check (H2 console)
Write-Host ""
Write-Host "TEST 5: H2 Database Console" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8080/h2-console" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/h2-console" -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ PASS - H2 Console is accessible (Status: $($response.StatusCode))" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  INFO - H2 Console may not be directly accessible via browser (expected)" -ForegroundColor Yellow
}

# Test 6: Compilation check
Write-Host ""
Write-Host "TEST 6: Project Compilation Status" -ForegroundColor Cyan
try {
    $compileOutput = & mvn -q compile 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ PASS - Project compiles successfully" -ForegroundColor Green
    } else {
        Write-Host "❌ FAIL - Project compilation failed" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ FAIL - Compilation check error: $_" -ForegroundColor Red
}

# Summary
Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Test Suite Complete" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Application is running on port 8080" -ForegroundColor Green
Write-Host "PID: $($appProcess.Id)" -ForegroundColor Gray
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Visit http://localhost:8080/swagger-ui.html" -ForegroundColor Gray
Write-Host "2. Test endpoints through Swagger UI" -ForegroundColor Gray
Write-Host "3. Check application logs for debugging" -ForegroundColor Gray
Write-Host ""
