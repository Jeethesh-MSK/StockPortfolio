# Stock Portfolio Manager - Buy/Sell Feature Guide

## 🎯 Overview

The Stock Portfolio Manager now includes a powerful Buy/Sell feature that allows users to manage their stock portfolio directly from the web interface. Users can purchase new shares or sell existing holdings with real-time price updates and automatic portfolio calculations.

## ✨ Features

### Buy Feature
- **Add New Stocks**: Purchase stocks not yet in your portfolio
- **Add More Shares**: Buy additional shares of stocks you already own
- **Custom Pricing**: Enter any price per share (useful for historical purchases or simulation)
- **Automatic Calculation**: Weighted average purchase price calculated automatically
- **Real-time Summary**: See total transaction cost before confirming

### Sell Feature
- **Partial Liquidation**: Sell any quantity of shares (up to your current holdings)
- **Full Liquidation**: Remove a stock completely from your portfolio
- **Current Market Price**: Uses live market prices for proceeds calculation
- **Validation**: Prevents overselling beyond your holdings
- **Real-time Feedback**: Shows remaining shares after partial sales

### User Experience
- 🎨 **Beautiful Modal Dialog**: Clean, intuitive transaction interface
- ⚡ **Instant Feedback**: Success and error messages in real-time
- 🔄 **Auto-Refresh**: Portfolio automatically updates after transactions
- 📱 **Responsive Design**: Works seamlessly on desktop and mobile
- ♿ **Accessibility**: Proper input validation and keyboard support

## 🚀 Getting Started

### Prerequisites
- Java 22 or higher
- Spring Boot 4.0.2
- React 18+
- Node.js and npm

### Running the Application

1. **Start the Backend**
   ```bash
   cd /path/to/StockPotrfolio
   java -jar target/StockPotrfolio-0.0.1-SNAPSHOT.jar
   ```
   Backend will be available at `http://localhost:8080`

2. **Start the Frontend**
   ```bash
   cd /path/to/StockPotrfolio/src/main/resources/static
   npm start
   ```
   Frontend will be available at `http://localhost:3000`

3. **Access the Application**
   Open your browser and navigate to `http://localhost:3000`

## 💡 How to Use

### Buying Stocks

1. **Click the Buy Button**
   - Locate a stock in your portfolio or the price lookup
   - Click the green "💰 Buy" button

2. **Enter Purchase Details**
   - **Quantity**: Number of shares you want to buy
   - **Price per Share**: Price you're paying per share

3. **Review Summary**
   - Check the total transaction cost
   - Verify the quantity and price

4. **Confirm Purchase**
   - Click the "Buy" button
   - Watch the success message
   - Your portfolio updates automatically

### Selling Stocks

1. **Click the Sell Button**
   - Find the stock in your portfolio
   - Click the red "📤 Sell" button

2. **Enter Sale Details**
   - **Quantity**: Number of shares you want to sell
   - Maximum is limited by your current holdings

3. **Review Summary**
   - Check the total proceeds at current market price
   - Verify the quantity

4. **Confirm Sale**
   - Click the "Sell" button
   - Watch the success message
   - Stock is removed or quantity updated automatically

## 🏗️ API Endpoints

### Buy Stock

**Endpoint**: `POST /api/portfolio/buy`

**Request Body**:
```json
{
  "symbol": "AAPL",
  "quantity": 10,
  "price": 150.50
}
```

**Response** (Success):
```json
{
  "status": "success",
  "message": "Stock purchase completed successfully",
  "symbol": "AAPL",
  "quantity": 10,
  "averagePrice": 150.50
}
```

**Response** (Error):
```json
{
  "error": "Validation Error",
  "message": "Stock symbol is required"
}
```

### Sell Stock

**Endpoint**: `POST /api/portfolio/sell`

**Request Body**:
```json
{
  "symbol": "AAPL",
  "quantity": 5
}
```

**Response** (Success):
```json
{
  "status": "success",
  "message": "Shares sold successfully. Remaining quantity: 5",
  "symbol": "AAPL",
  "quantity": 5,
  "averagePrice": 150.50
}
```

**Response** (Error):
```json
{
  "error": "Validation Error",
  "message": "Insufficient shares. You have 10 shares but trying to sell 20"
}
```

## 📊 How It Works Behind the Scenes

### Weighted Average Price Calculation

When buying additional shares of a stock you already own, the application calculates a weighted average purchase price:

```
New Average Price = (OldQuantity × OldPrice + NewQuantity × NewPrice) / (OldQuantity + NewQuantity)
```

**Example**:
- Current holding: 10 shares at $150
- New purchase: 5 shares at $160
- New average: (10×150 + 5×160) / (10+5) = (1500+800) / 15 = 153.33

### Inventory Management

**Complete Sale**: When you sell all shares of a stock, the portfolio item is removed entirely from the database.

**Partial Sale**: When you sell some (but not all) shares, the quantity is updated and the original average purchase price is retained.

## ⚠️ Error Handling

The application validates all inputs and provides helpful error messages:

| Error | Resolution |
|-------|-----------|
| "Stock symbol is required" | Enter a valid stock symbol |
| "Quantity must be greater than 0" | Enter a positive whole number |
| "Price must be greater than 0" | Enter a valid positive price |
| "Stock symbol not found in portfolio" | You can only sell stocks you own |
| "Insufficient shares" | You don't have enough shares to sell |

## 🧪 Testing

### Manual Testing

1. **Test Buy with New Stock**
   - Click "💰 Buy" on a stock
   - Enter quantity and price
   - Verify portfolio item is created

2. **Test Buy with Existing Stock**
   - Buy the same stock twice at different prices
   - Verify weighted average price is calculated correctly

3. **Test Partial Sell**
   - Buy 10 shares of a stock
   - Sell 5 shares
   - Verify 5 shares remain with same average price

4. **Test Complete Sell**
   - Buy 10 shares of a stock
   - Sell all 10 shares
   - Verify stock is removed from portfolio

5. **Test Validation**
   - Try to sell more shares than you own
   - Try to enter invalid numbers
   - Verify error messages appear

### Automated Tests

Run the test suite:
```bash
npm test -- PortfolioItem.BuySell.test.js
```

## 📁 File Structure

```
StockPotrfolio/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/stockpotrfolio/
│   │   │       ├── controller/
│   │   │       │   └── PortfolioController.java    (Updated)
│   │   │       └── service/
│   │   │           └── PortfolioService.java       (Updated)
│   │   └── resources/
│   │       └── static/src/
│   │           ├── api/
│   │           │   └── portfolioAPI.js             (Updated)
│   │           ├── components/
│   │           │   ├── PortfolioItem.js            (Updated)
│   │           │   ├── PortfolioItem.test.js
│   │           │   ├── PortfolioItem.BuySell.test.js (New)
│   │           │   └── PortfolioList.js            (Updated)
│   │           └── styles/
│   │               └── PortfolioItem.css           (Updated)
│   └── test/
│       └── java/
│           └── org/example/stockpotrfolio/
```

## 🎨 UI Components

### Buy/Sell Button Styles
- Green button for Buy actions (#4CAF50)
- Red button for Sell actions (#F44336)
- Hover effects and active states
- Responsive sizing for mobile devices

### Modal Dialog
- Semi-transparent overlay background
- Smooth slide-up animation on open
- Close button (×) in header
- Form validation with real-time feedback
- Transaction summary panel
- Confirm/Cancel action buttons

### Messages
- Green success messages with checkmark
- Red error messages with details
- Loading indicator during processing
- Auto-dismiss after 2 seconds on success

## 💾 Data Persistence

All transactions are persisted to the H2 database:
- Each buy/sell updates the database immediately
- Portfolio items are stored with symbol, quantity, and average buy price
- Historical data is maintained for profit/loss calculations

## 🔒 Validation Rules

### Buy Validation
- ✓ Symbol is required and non-empty
- ✓ Quantity must be > 0
- ✓ Price must be > 0
- ✓ All inputs are trimmed and uppercased

### Sell Validation
- ✓ Symbol is required and non-empty
- ✓ Quantity must be > 0
- ✓ Stock must exist in portfolio
- ✓ Quantity ≤ current holdings

## 🚨 Known Limitations

1. **Price Entry**: Users must manually enter prices when buying (no automatic historical price lookup)
2. **Currency**: Fixed in USD - no multi-currency support
3. **Decimal Places**: Prices are limited to 2 decimal places
4. **Transaction History**: Individual transaction history is not tracked (only current holdings)

## 🔮 Future Enhancements

Potential improvements for future versions:

- [ ] Transaction history/audit trail
- [ ] Batch operations (buy/sell multiple stocks at once)
- [ ] Historical price data integration
- [ ] Tax lot accounting (FIFO, LIFO, average cost)
- [ ] Portfolio export (CSV, PDF)
- [ ] Recurring transactions/scheduled buys
- [ ] Multi-currency support
- [ ] Commission/fee tracking
- [ ] Target allocation management
- [ ] Price alerts

## 📧 Support

For issues or questions:
1. Check the error message - it usually describes the problem
2. Ensure the backend API is running on port 8080
3. Clear browser cache if UI doesn't update
4. Check browser console for detailed error logs

## 📝 License

This feature is part of the Stock Portfolio Manager project.

---

**Last Updated**: February 2, 2026
**Version**: 1.0
