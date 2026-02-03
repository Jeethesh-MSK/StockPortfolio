# 🚀 Quick Start: Buy/Sell Feature

## What's New?

Your Stock Portfolio Manager now has **Buy** 💰 and **Sell** 📤 buttons! You can now easily purchase stocks and liquidate positions directly from the portfolio interface.

## Quick Setup

### 1. Build the Project
```bash
cd C:\Users\Administrator\JavaLearning\StockPotrfolio
mvn clean package -DskipTests
```

### 2. Start the Backend
```bash
java -jar target/StockPotrfolio-0.0.1-SNAPSHOT.jar
```
✅ Wait for: "Tomcat started on port(s): 8080"

### 3. Open in Browser
Navigate to `http://localhost:3000`

## Using Buy/Sell

### 🟢 BUY Stocks
1. Click **💰 Buy** button on any stock card
2. Enter:
   - **Quantity**: How many shares
   - **Price**: Price per share
3. Review the **Total Cost** summary
4. Click **Buy** to confirm
5. ✅ Portfolio updates automatically!

### 🔴 SELL Stocks
1. Click **📤 Sell** button on any stock
2. Enter:
   - **Quantity**: How many shares to sell (max = your holdings)
3. Review the **Total Proceeds** (at current market price)
4. Click **Sell** to confirm
5. ✅ Shares sold! Stock removed or quantity updated

## Key Features

✅ **Weighted Average Pricing** - Automatically calculates average cost when buying more
✅ **Real-time Updates** - Portfolio refreshes immediately after transaction
✅ **Input Validation** - Can't oversell or enter invalid prices
✅ **Beautiful Modal** - Clean, intuitive transaction dialog
✅ **Error Messages** - Clear feedback if something goes wrong
✅ **Mobile Friendly** - Works on phones and tablets

## Files Changed

**Backend** (Java/Spring Boot):
- `PortfolioService.java` - Added `sellStock()` method
- `PortfolioController.java` - Added `/buy` and `/sell` endpoints

**Frontend** (React):
- `portfolioAPI.js` - Added `buyStock()` and `sellStock()` functions
- `PortfolioItem.js` - Added buttons, modal, and transaction logic
- `PortfolioList.js` - Added auto-refresh callback
- `PortfolioItem.css` - Styled buttons and modal

## Example Scenarios

### Scenario 1: Buy Your First Stock
```
1. Click "💰 Buy" on MSFT
2. Enter: Quantity = 5, Price = $380.25
3. See: Total Cost = $1,901.25
4. Click "Buy"
5. Success! 5 MSFT shares added to portfolio
```

### Scenario 2: Buy More of a Stock You Already Own
```
Current: 10 AAPL shares at $150 average
Action: Buy 5 more at $160
Result: 15 AAPL shares at $153.33 average (weighted)
```

### Scenario 3: Sell Some Shares
```
Current: 20 GOOGL shares
Action: Sell 8 shares
Result: 12 GOOGL shares remain
```

### Scenario 4: Sell All Shares (Complete Exit)
```
Current: 10 TSLA shares
Action: Sell 10 shares
Result: TSLA removed from portfolio entirely
```

## Troubleshooting

### Issue: Can't see Buy/Sell buttons
- **Solution**: Refresh the page (Ctrl+R or Cmd+R)
- **Check**: Make sure backend is running on http://localhost:8080

### Issue: "Error: Port 8080 already in use"
- **Solution**: Stop other Java processes or use a different port
- **Quick Fix**: 
  ```bash
  # Windows PowerShell
  Get-Process java | Stop-Process -Force
  ```

### Issue: Can't sell - "Insufficient shares"
- **Solution**: You're trying to sell more than you own
- **Check**: Verify the quantity is less than or equal to your holdings

### Issue: Modal closes without saving
- **Solution**: Check browser console for error details
- **Try**: Refresh page and try again

## API Reference

### Buy Stock
```
POST /api/portfolio/buy
Content-Type: application/json

{
  "symbol": "AAPL",
  "quantity": 10,
  "price": 150.50
}
```

### Sell Stock
```
POST /api/portfolio/sell
Content-Type: application/json

{
  "symbol": "AAPL",
  "quantity": 5
}
```

## Testing Checklist

- [ ] Can click Buy button and open modal
- [ ] Can enter quantity and price
- [ ] Modal shows transaction summary
- [ ] Can click Confirm and buy succeeds
- [ ] Portfolio refreshes with new stock/quantity
- [ ] Can click Sell button and open modal
- [ ] Cannot sell more than you own
- [ ] Can sell partial quantity
- [ ] Can sell all shares (stock removed)
- [ ] Success messages display
- [ ] Error messages are helpful
- [ ] Modal closes after transaction

## Next Steps

1. **Test It Out** - Try buying and selling a few stocks
2. **Read the Full Guide** - See `BUY_SELL_GUIDE.md` for detailed docs
3. **Check the Tests** - Run `npm test` to see test examples
4. **Explore the Code** - All files are well-commented

## Support Files

- 📄 `BUY_SELL_FEATURE.md` - Technical implementation details
- 📄 `BUY_SELL_GUIDE.md` - Complete user guide
- 🧪 `PortfolioItem.BuySell.test.js` - Test examples

---

**Ready to go!** 🎉 Start buying and selling stocks now!
