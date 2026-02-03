# Stock Portfolio Application - Testing Guide

## Overview

This document provides comprehensive testing instructions for both the frontend and backend of the Stock Portfolio application.

## Prerequisites

- Java 17 or higher
- Node.js v14 or higher
- npm v6 or higher
- Spring Boot 4.0.2
- React 18.2.0

## Quick Start

### Option 1: Using Startup Script (Windows)
```powershell
cd C:\Users\Administrator\JavaLearning\StockPotrfolio
.\startup.bat
```

### Option 2: Using Startup Script (Linux/Mac)
```bash
cd ~/JavaLearning/StockPotrfolio
chmod +x startup.sh
./startup.sh
```

### Option 3: Manual Start

**Terminal 1 - Backend:**
```powershell
cd C:\Users\Administrator\JavaLearning\StockPotrfolio
java -jar target\StockPotrfolio-0.0.1-SNAPSHOT.jar
```

**Terminal 2 - Frontend:**
```powershell
cd C:\Users\Administrator\JavaLearning\StockPotrfolio\src\main\resources\static
npm start
```

## Backend Testing

### 1. Build the Backend

```powershell
cd C:\Users\Administrator\JavaLearning\StockPotrfolio
mvn clean package -DskipTests
```

### 2. Start the Backend Server

```powershell
java -jar target\StockPotrfolio-0.0.1-SNAPSHOT.jar
```

**Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/

[main] c.e.s.StockPotrfolioApplication : Started StockPotrfolioApplication in X.XXX seconds
```

### 3. API Testing

#### Test Portfolio Endpoint

```bash
curl -X GET http://localhost:8080/api/portfolio
```

**Expected Response:**
```json
{
  "portfolio": [
    {
      "symbol": "AAPL",
      "quantity": 10,
      "averageBuyPrice": 150.0,
      "currentLivePrice": 165.0,
      "profitOrLossPercentage": 10.0
    }
  ]
}
```

#### Test Stock Price Endpoint

```bash
curl -X GET http://localhost:8080/api/stocks/price/AAPL
```

**Expected Response:**
```json
{
  "symbol": "AAPL",
  "price": 165.0,
  "timestamp": "2024-02-02T10:30:00Z"
}
```

### 4. Swagger UI Documentation

Access the interactive API documentation:
- URL: http://localhost:8080/swagger-ui.html
- You can test all endpoints directly from the browser

### 5. H2 Database Console

Access the in-memory database console:
- URL: http://localhost:8080/h2-console
- Driver Class: `org.h2.Driver`
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: (leave empty)

## Frontend Testing

### 1. Install Dependencies

```powershell
cd C:\Users\Administrator\JavaLearning\StockPotrfolio\src\main\resources\static
npm install
```

### 2. Start the Development Server

```powershell
npm start
```

The application will automatically open in your browser at `http://localhost:3000`

### 3. Manual UI Testing

#### Portfolio Dashboard Tests

1. **Initial Load**
   - ✅ The page loads without errors
   - ✅ "Loading portfolio..." spinner appears briefly
   - ✅ Portfolio items are displayed after loading

2. **Portfolio Summary Cards**
   - ✅ "Total Portfolio Value" displays correct sum of all holdings
   - ✅ "Total Investment" displays total cost basis
   - ✅ "Total Gain/Loss" shows profit/loss and percentage
   - ✅ Colors change (green for profit, red for loss)

3. **Portfolio Items**
   - ✅ Each stock symbol is displayed
   - ✅ Quantity in shares is shown
   - ✅ Average buy price is displayed correctly
   - ✅ Current live price is shown
   - ✅ Total cost (quantity × avg price) is calculated
   - ✅ Current value (quantity × current price) is calculated
   - ✅ Profit/Loss amount is calculated correctly
   - ✅ Profit/Loss percentage is accurate

4. **Refresh Functionality**
   - ✅ Click "Refresh" button
   - ✅ Button text changes to "Refreshing..."
   - ✅ Button is disabled while refreshing
   - ✅ "Last updated" timestamp updates

5. **Auto-Refresh**
   - ✅ Portfolio auto-refreshes every 30 seconds
   - ✅ Live prices update automatically

#### Stock Price Lookup Tests

1. **Search Functionality**
   - ✅ Enter valid symbol "AAPL"
   - ✅ Click "Search" button
   - ✅ Current price is displayed
   - ✅ Timestamp shows when price was fetched

2. **Error Handling**
   - ✅ Leave symbol blank and click search
   - ✅ Error message appears: "Please enter a stock symbol"
   - ✅ Enter invalid symbol "INVALIDXYZ"
   - ✅ Error message appears: "Failed to fetch price"

3. **Input Validation**
   - ✅ Symbol is converted to uppercase automatically
   - ✅ Input field is disabled while searching
   - ✅ Search button is disabled while searching

### 4. Automated Testing

#### Run Unit Tests

```powershell
cd C:\Users\Administrator\JavaLearning\StockPotrfolio\src\main\resources\static
npm test
```

#### Test Coverage

The following test suites are included:

1. **PortfolioItem.test.js**
   - Renders portfolio item with correct data
   - Displays all financial metrics
   - Calculates profit/loss correctly
   - Shows profit/loss badges

2. **PortfolioList.test.js**
   - Renders portfolio list with header
   - Displays loading state
   - Fetches and displays portfolio items
   - Calculates portfolio summary correctly
   - Handles API errors gracefully
   - Refresh button works correctly

3. **StockPriceFetcher.test.js**
   - Renders search component
   - Validates input fields
   - Displays stock prices
   - Handles search errors
   - Disables inputs while searching

4. **portfolioAPI.test.js**
   - Tests portfolio API integration
   - Tests stock price API integration
   - Handles API errors

### 5. Responsive Design Testing

Test on different screen sizes:

1. **Desktop (1920×1080)**
   - ✅ Two-column layout (portfolio left, sidebar right)
   - ✅ All elements clearly visible

2. **Tablet (768×1024)**
   - ✅ Grid adjusts properly
   - ✅ Sidebar remains accessible
   - ✅ No horizontal scrolling

3. **Mobile (375×812)**
   - ✅ Single column layout
   - ✅ Touch-friendly button sizes
   - ✅ Text is readable
   - ✅ No overflow issues

### 6. Browser Compatibility

Test in the following browsers:

- ✅ Chrome/Edge (latest)
- ✅ Firefox (latest)
- ✅ Safari (latest)

## Integration Testing

### Full Application Flow

1. **Start Backend**
   ```
   Backend running on http://localhost:8080
   ```

2. **Start Frontend**
   ```
   Frontend running on http://localhost:3000
   ```

3. **Verify Communication**
   - ✅ Open frontend in browser
   - ✅ Portfolio loads with live data
   - ✅ Refresh button updates data from backend
   - ✅ Stock price lookup calls backend API

4. **Test API Calls**
   - ✅ Check browser DevTools Network tab
   - ✅ Verify requests go to `http://localhost:8080/api/*`
   - ✅ Check response status codes are 200
   - ✅ Verify response data is valid JSON

### CORS Testing

1. **Verify CORS Headers**
   - ✅ Frontend requests include proper Origin header
   - ✅ Backend responses include proper CORS headers
   - ✅ No CORS errors in browser console

## Performance Testing

### Frontend Performance

1. **Initial Load Time**
   - ✅ Page loads in < 3 seconds
   - ✅ No layout shifts after load

2. **Refresh Performance**
   - ✅ Portfolio refresh completes in < 1 second
   - ✅ UI remains responsive during refresh

3. **Search Performance**
   - ✅ Stock price search completes in < 2 seconds

### Backend Performance

1. **Portfolio Endpoint**
   - ✅ Response time < 500ms
   - ✅ Can handle 10+ stocks without issue

2. **Stock Price Endpoint**
   - ✅ Response time < 1000ms (includes Finnhub API call)
   - ✅ Properly handles invalid symbols

## Debugging

### Enable Logging

**Backend:** Edit `application.properties`
```properties
logging.level.root=INFO
logging.level.org.example.stockpotrfolio=DEBUG
```

**Frontend:** Browser DevTools
- F12 to open DevTools
- Console tab for errors
- Network tab for API calls
- Application tab for stored data

### Common Issues

1. **"Failed to fetch portfolio" Error**
   - Check backend is running: `http://localhost:8080`
   - Check CORS is enabled in Spring Boot
   - Check browser console for specific error

2. **Stock prices not updating**
   - Verify Finnhub API key is configured
   - Check if API rate limit is exceeded
   - Look for errors in backend logs

3. **Frontend not starting**
   - Delete `node_modules` and run `npm install` again
   - Clear npm cache: `npm cache clean --force`
   - Check port 3000 is not in use

## Test Results Checklist

- [ ] Backend builds successfully
- [ ] Backend starts without errors
- [ ] Frontend dependencies install
- [ ] Frontend starts without errors
- [ ] Portfolio loads with data
- [ ] Portfolio summary calculations are correct
- [ ] Stock price lookup works
- [ ] Refresh button updates data
- [ ] Error messages display correctly
- [ ] Responsive design works on all screen sizes
- [ ] All unit tests pass
- [ ] No CORS errors in console
- [ ] API responses are valid
- [ ] Database console is accessible

## Conclusion

If all tests pass, your Stock Portfolio application is ready for use! 🎉

For any issues or questions, check the individual README files:
- Backend: See main `README.md`
- Frontend: See `src/main/resources/static/README.md`
