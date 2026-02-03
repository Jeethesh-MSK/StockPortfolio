# Stock Portfolio Frontend

This is the React frontend for the Stock Portfolio application.

## Features

- **Portfolio Dashboard**: View all your stock holdings with live prices
- **Performance Metrics**: Track profit/loss for each stock and overall portfolio
- **Stock Price Lookup**: Search for current prices of any stock symbol
- **Real-time Updates**: Auto-refresh portfolio every 30 seconds
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices

## Project Structure

```
src/
├── components/
│   ├── PortfolioItem.js       # Individual stock item component
│   ├── PortfolioList.js       # Portfolio list with summary
│   ├── StockPriceFetcher.js   # Stock price search component
│   ├── *.test.js              # Component tests
├── api/
│   ├── portfolioAPI.js        # API service
│   └── portfolioAPI.test.js   # API tests
├── styles/
│   ├── PortfolioItem.css
│   ├── PortfolioList.css
│   ├── StockPriceFetcher.css
│   └── App.css
├── App.js                      # Main application component
├── App.css                     # Application styles
├── index.js                    # React entry point
└── index.css                   # Global styles
```

## Setup and Installation

### Prerequisites
- Node.js (v14 or higher)
- npm (v6 or higher)

### Installation Steps

1. Navigate to the frontend directory:
```bash
cd src/main/resources/static
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

The application will open at `http://localhost:3000`

## Backend Requirements

Make sure the Spring Boot backend is running on `http://localhost:8080` with the following endpoints:
- `GET /api/portfolio` - Get all portfolio items with live prices
- `GET /api/stocks/price/{symbol}` - Get current price for a stock symbol

## Available Scripts

### `npm start`
Runs the app in development mode. Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

### `npm test`
Launches the test runner in interactive watch mode.

### `npm run build`
Builds the app for production to the `build` folder.

## API Integration

### getPortfolio()
Fetches the complete portfolio with live prices from the backend.

**Response:**
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

### getStockPrice(symbol)
Fetches the current price for a specific stock symbol.

**Response:**
```json
{
  "symbol": "AAPL",
  "price": 165.0,
  "timestamp": "2024-02-02T10:30:00Z"
}
```

## Components

### PortfolioItem
Displays a single stock holding with detailed information including:
- Stock symbol and quantity
- Average buy price and current price
- Total cost and current value
- Profit/loss amount and percentage

### PortfolioList
Main component that:
- Fetches and displays all portfolio items
- Shows portfolio summary with total value, investment, and gain/loss
- Auto-refreshes every 30 seconds
- Handles loading and error states

### StockPriceFetcher
Sidebar component for:
- Searching stock prices by symbol
- Displaying current price information
- Handling search errors gracefully

## Styling

The application uses custom CSS with a modern gradient design:
- Purple gradient background (#667eea to #764ba2)
- Responsive grid layouts
- Smooth animations and transitions
- Color-coded profit (green) and loss (red) indicators

## Testing

The frontend includes comprehensive test suites for:
- Component rendering
- API integration
- Error handling
- User interactions

Run tests with:
```bash
npm test
```

## Performance Optimization

- Auto-refresh interval: 30 seconds
- Lazy component rendering
- Memoization of expensive calculations
- CSS animations for smooth UX

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

### Backend Connection Issues
If you see "Failed to fetch portfolio" error:
1. Ensure Spring Boot backend is running on http://localhost:8080
2. Check CORS configuration in the backend
3. Verify API endpoints are accessible

### Stock Symbol Not Found
- Ensure the symbol is valid (e.g., AAPL, GOOGL, MSFT)
- Check that the Finnhub API is properly configured in the backend

## License

This project is part of the Stock Portfolio Learning Application.
