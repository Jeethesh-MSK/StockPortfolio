# Buy/Sell Feature Implementation

## Overview
Added Buy and Sell buttons to the Stock Portfolio Manager application, enabling users to easily purchase and sell stock shares directly from the portfolio interface.

## Changes Made

### Backend Changes

#### 1. PortfolioService.java
**Added:** `sellStock(String symbol, Integer quantity)` method
- Validates input parameters
- Checks if stock exists in portfolio
- Verifies sufficient shares are available
- Removes portfolio item if all shares are sold
- Updates quantity if partial sale
- Returns updated PortfolioItem (or null if completely sold)

#### 2. PortfolioController.java
**Added Endpoints:**

- **POST /api/portfolio/buy**
  - Request body: `{ symbol, quantity, price }`
  - Returns: `TransactionResponse` with success message
  - Validates all inputs
  - Handles errors gracefully

- **POST /api/portfolio/sell**
  - Request body: `{ symbol, quantity }`
  - Returns: `TransactionResponse` with success message
  - Validates inputs and availability
  - Handles errors gracefully

**Added Classes:**
- `TransactionRequest`: DTO for buy requests (symbol, quantity, price)
- `SellRequest`: DTO for sell requests (symbol, quantity)
- `TransactionResponse`: DTO for transaction responses

### Frontend Changes

#### 1. portfolioAPI.js
**Added Functions:**
- `buyStock(symbol, quantity, price)`: POST request to /portfolio/buy
- `sellStock(symbol, quantity)`: POST request to /portfolio/sell

#### 2. PortfolioItem.js
**Major Updates:**
- Added buy/sell button UI with icons
- Implemented transaction modal dialog
- Added form for quantity and price input
- Shows transaction summary (total cost/proceeds)
- Real-time error and success messages
- Automatic portfolio refresh after successful transaction
- State management for modal visibility, loading, and errors

**Key Features:**
- Input validation (quantity range, price validation)
- Prevents selling more shares than owned
- Calculates total transaction amount
- Shows success/error messages in modal
- Auto-closes modal after successful transaction

#### 3. PortfolioList.js
**Updated:**
- Passes `onTransactionComplete` callback to PortfolioItem
- Callback triggers `fetchPortfolio()` to refresh portfolio data

#### 4. PortfolioItem.css
**Added Styles:**
- `.portfolio-item-actions`: Container for buy/sell buttons
- `.btn-buy`: Green buy button styling
- `.btn-sell`: Red sell button styling
- `.modal-overlay`: Full-screen modal background
- `.modal-content`: Modal dialog box with animations
- `.modal-header`: Title and close button
- `.modal-body`: Form inputs and summary
- `.modal-actions`: Confirm/Cancel buttons
- `.form-group`: Input field styling
- `.transaction-summary`: Transaction details preview
- `.modal-error` / `.modal-success`: Status messages
- Responsive mobile styling

## How It Works

### Buy Flow
1. User clicks "💰 Buy" button on a stock
2. Modal opens showing buy form
3. User enters quantity and price per share
4. Summary shows total cost
5. User confirms the purchase
6. API sends POST to `/api/portfolio/buy`
7. Backend updates portfolio with weighted average price calculation
8. Success message displays
9. Portfolio refreshes automatically

### Sell Flow
1. User clicks "📤 Sell" button on a stock
2. Modal opens showing sell form
3. User enters quantity (max is current holdings)
4. Summary shows total proceeds at current market price
5. User confirms the sale
6. API sends POST to `/api/portfolio/sell`
7. Backend removes item if all shares sold, or updates quantity
8. Success message displays
9. Portfolio refreshes automatically

## Error Handling

### Validation
- Non-empty stock symbol
- Positive quantity and price
- Sufficient shares for selling
- Valid numeric inputs

### User Feedback
- Clear error messages in modal
- Success confirmation messages
- Disabled buttons while processing
- Loading state indicator

## Testing

To test the new functionality:

1. **Start the application**
   ```
   java -jar target/StockPotrfolio-0.0.1-SNAPSHOT.jar
   ```

2. **Access the UI**
   Navigate to `http://localhost:3000` (or where frontend is served)

3. **Add a stock first**
   Use the Stock Price Lookup to find a stock and purchase it

4. **Test Buy**
   - Click "💰 Buy" on an existing stock
   - Enter quantity and price
   - Verify portfolio is updated
   - Verify weighted average price is calculated

5. **Test Sell**
   - Click "📤 Sell" on a stock
   - Enter quantity (test full and partial sells)
   - Verify portfolio item is removed (full) or updated (partial)

## API Examples

### Buy Request
```json
POST /api/portfolio/buy
{
  "symbol": "AAPL",
  "quantity": 5,
  "price": 150.25
}
```

### Sell Request
```json
POST /api/portfolio/sell
{
  "symbol": "AAPL",
  "quantity": 3
}
```

## Features

✅ Buy stocks with custom price
✅ Sell stocks from portfolio
✅ Automatic weighted average price calculation
✅ Remove portfolio items when all shares sold
✅ Real-time portfolio updates
✅ Transaction validation and error handling
✅ User-friendly modal interface
✅ Responsive design
✅ Success/error feedback messages
✅ Loading states during transactions

## Files Modified

1. `/src/main/java/org/example/stockpotrfolio/service/PortfolioService.java`
2. `/src/main/java/org/example/stockpotrfolio/controller/PortfolioController.java`
3. `/src/main/resources/static/src/api/portfolioAPI.js`
4. `/src/main/resources/static/src/components/PortfolioItem.js`
5. `/src/main/resources/static/src/components/PortfolioList.js`
6. `/src/main/resources/static/src/styles/PortfolioItem.css`
