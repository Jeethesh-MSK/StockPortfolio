# Stock Portfolio Application - Architecture & Design

## Project Overview

The Stock Portfolio Application is a full-stack web application for managing and tracking stock investments. It provides real-time portfolio analysis, live stock price tracking, and profit/loss calculations.

## Technology Stack

### Backend
- **Framework:** Spring Boot 4.0.2
- **Language:** Java 17
- **Database:** H2 (In-Memory)
- **ORM:** Hibernate/JPA
- **API Documentation:** Swagger/OpenAPI 3.0
- **External API:** Finnhub (Stock Prices)

### Frontend
- **Framework:** React 18.2.0
- **Language:** JavaScript (ES6+)
- **HTTP Client:** Axios
- **Testing:** Jest & React Testing Library
- **Styling:** CSS3
- **Build Tool:** Create React App

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (React)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    App Component                     │  │
│  │  ┌──────────────────┐  ┌──────────────────────────┐ │  │
│  │  │ PortfolioList    │  │ StockPriceFetcher       │ │  │
│  │  │ - View portfolio │  │ - Search stock prices   │ │  │
│  │  │ - Summary cards  │  │ - Display price info    │ │  │
│  │  │ - Refresh data   │  │ - Handle errors         │ │  │
│  │  └────────┬─────────┘  └──────────┬───────────────┘ │  │
│  └───────────┼─────────────────────────┼────────────────┘  │
│              │                         │                   │
└──────────────┼─────────────────────────┼───────────────────┘
               │                         │
      axios/HTTP (CORS)         axios/HTTP (CORS)
               │                         │
┌──────────────┼─────────────────────────┼───────────────────┐
│              ▼                         ▼                   │
│  ┌──────────────────────────────────────────────────────┐ │
│  │           Spring Boot Backend (Port 8080)           │ │
│  │                                                      │ │
│  │  ┌──────────────────────────────────────────────┐  │ │
│  │  │           REST Controllers                   │  │ │
│  │  │  ┌────────────┐      ┌─────────────────┐    │  │ │
│  │  │  │ Portfolio  │      │ Stock           │    │  │ │
│  │  │  │ Controller │      │ Controller      │    │  │ │
│  │  │  └────────┬───┘      └────────┬────────┘    │  │ │
│  │  └──────────┼───────────────────┼─────────────┘  │ │
│  │             │                   │                │ │
│  │  ┌──────────┼───────────────────┼─────────────┐  │ │
│  │  │          ▼                   ▼             │  │ │
│  │  │    Service Layer             │             │  │ │
│  │  │  ┌─────────────┐      ┌──────▼─────┐      │  │ │
│  │  │  │ Portfolio   │      │ StockPrice │      │  │ │
│  │  │  │ Service     │      │ Service    │      │  │ │
│  │  │  └────────┬────┘      └──────┬─────┘      │  │ │
│  │  │           │                  │            │  │ │
│  │  │  ┌────────┴──────────┬───────┴───────┐   │  │ │
│  │  │  │                   │               │   │  │ │
│  │  │  ▼                   ▼               ▼   │  │ │
│  │  │ Repository       H2 Database    Finnhub │  │ │
│  │  │ (PortfolioItem)  (testdb)       API     │  │ │
│  │  └───────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────┘ │
│                                                  │
└──────────────────────────────────────────────────┘
```

## Core Components

### Backend Architecture

#### 1. Controllers Layer
**Location:** `src/main/java/org/example/stockpotrfolio/controller/`

- **PortfolioController**
  - Endpoint: `GET /api/portfolio`
  - Returns all portfolio items with live prices
  - Integrates with PortfolioService and StockPriceService

- **StockController**
  - Endpoint: `GET /api/stocks/price/{symbol}`
  - Returns current price for a given symbol
  - Fetches from Finnhub API via StockPriceService

#### 2. Service Layer
**Location:** `src/main/java/org/example/stockpotrfolio/service/`

- **PortfolioService**
  - Retrieves all portfolio items from database
  - No business logic; acts as facade for repository

- **StockPriceService**
  - Fetches live prices from Finnhub API
  - Handles API errors gracefully
  - Caches results briefly if needed

#### 3. Repository Layer
**Location:** `src/main/java/org/example/stockpotrfolio/repository/`

- **PortfolioRepository**
  - Extends JpaRepository<PortfolioItem, Long>
  - Provides CRUD operations for portfolio items
  - Connected to H2 in-memory database

#### 4. Data Models
**Location:** `src/main/java/org/example/stockpotrfolio/entity/` and `dto/`

- **PortfolioItem** (Entity)
  - Represents a stock holding in the database
  - Fields: id, symbol, quantity, averageBuyPrice

- **PortfolioSummary** (Record)
  - Data Transfer Object for API responses
  - Fields: symbol, quantity, averageBuyPrice, currentLivePrice, profitOrLossPercentage
  - Includes validation and factory method

### Frontend Architecture

#### 1. Component Structure

```
src/
├── components/
│   ├── PortfolioList.js
│   │   ├── Fetches portfolio from backend
│   │   ├── Manages loading/error states
│   │   ├── Calculates portfolio summary
│   │   └── Renders PortfolioItem components
│   │
│   ├── PortfolioItem.js
│   │   ├── Displays single stock holding
│   │   ├── Calculates profit/loss
│   │   └── Applies profit/loss styling
│   │
│   └── StockPriceFetcher.js
│       ├── Handles stock symbol search
│       ├── Fetches price from backend
│       └── Displays stock price info
│
├── api/
│   └── portfolioAPI.js
│       ├── getPortfolio()
│       └── getStockPrice(symbol)
│
├── styles/
│   ├── App.css
│   ├── PortfolioList.css
│   ├── PortfolioItem.css
│   └── StockPriceFetcher.css
│
├── App.js (Main component)
└── index.js (React entry point)
```

#### 2. Data Flow

```
User Interaction
       │
       ▼
Component State Update
       │
       ▼
API Call (portfolioAPI.js)
       │
       ▼
HTTP Request to Backend
       │
       ▼
Backend Processing
       │
       ▼
API Response
       │
       ▼
Update Component State
       │
       ▼
Re-render Component
       │
       ▼
Display Updated UI
```

## Data Models

### Entity-Relationship Diagram

```
┌─────────────────────────────┐
│      PortfolioItem          │
├─────────────────────────────┤
│ id (Primary Key)            │
│ symbol (String)             │
│ quantity (Integer)          │
│ averageBuyPrice (Double)    │
└─────────────────────────────┘
         │
         │ Mapped to
         │
         ▼
┌──────────────────────────────────────┐
│      PortfolioSummary (DTO)          │
├──────────────────────────────────────┤
│ symbol (String)                      │
│ quantity (Integer)                   │
│ averageBuyPrice (Double)             │
│ currentLivePrice (Double)            │
│ profitOrLossPercentage (Double)      │
└──────────────────────────────────────┘
```

## API Endpoints

### Portfolio Endpoints

#### GET /api/portfolio
**Purpose:** Retrieve all portfolio items with live prices

**Request:**
```
GET http://localhost:8080/api/portfolio
```

**Response (200 OK):**
```json
{
  "portfolio": [
    {
      "symbol": "AAPL",
      "quantity": 10,
      "averageBuyPrice": 150.0,
      "currentLivePrice": 165.0,
      "profitOrLossPercentage": 10.0
    },
    {
      "symbol": "GOOGL",
      "quantity": 5,
      "averageBuyPrice": 2800.0,
      "currentLivePrice": 2900.0,
      "profitOrLossPercentage": 3.57
    }
  ]
}
```

**Error Response (500):**
```json
{
  "error": "Error fetching portfolio or live prices",
  "message": "Failed to fetch price for AAPL"
}
```

### Stock Price Endpoints

#### GET /api/stocks/price/{symbol}
**Purpose:** Retrieve current price for a stock symbol

**Request:**
```
GET http://localhost:8080/api/stocks/price/AAPL
```

**Response (200 OK):**
```json
{
  "symbol": "AAPL",
  "price": 165.0,
  "timestamp": "2024-02-02T10:30:00Z"
}
```

**Error Response (404 or 400):**
```json
{
  "error": "Failed to fetch price",
  "message": "Invalid stock symbol: INVALIDXYZ"
}
```

## Key Design Patterns

### 1. Layered Architecture
- **Separation of Concerns:** Each layer has a specific responsibility
- **Testability:** Easy to unit test each layer independently
- **Maintainability:** Changes in one layer don't affect others

### 2. Dependency Injection
- Spring's `@Autowired` annotation injects dependencies
- Reduces coupling between classes
- Makes code more testable

### 3. Data Transfer Objects (DTOs)
- `PortfolioSummary` decouples internal representation from API response
- Provides schema validation via record compact constructor
- Includes factory method for consistent object creation

### 4. Service Facade Pattern
- Controllers delegate to services
- Services handle business logic and orchestration
- Repositories handle data persistence

### 5. Reactive UI Updates
- React components manage their own state
- Auto-refresh mechanism updates data every 30 seconds
- Error handling provides user feedback

## Database Schema

### H2 Database Tables

```sql
CREATE TABLE portfolio_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    symbol VARCHAR(10) NOT NULL,
    quantity INTEGER NOT NULL,
    average_buy_price DOUBLE NOT NULL
);
```

### Sample Data
The database is populated with sample data on startup (H2 with `create-drop` DDL mode).

## Error Handling

### Backend Error Handling
- Controllers catch exceptions and return appropriate HTTP status codes
- 500 errors for server-side failures
- Logging at WARN/ERROR levels for debugging
- User-friendly error messages in responses

### Frontend Error Handling
- Try-catch blocks in async API calls
- User-friendly error messages displayed in UI
- Loading states prevent multiple simultaneous requests
- Graceful fallbacks (e.g., using average buy price if live price unavailable)

## Performance Considerations

### Backend Optimization
- Database queries optimized with proper indexing
- API responses include only necessary data
- RestTemplate for efficient HTTP communication

### Frontend Optimization
- Auto-refresh interval set to 30 seconds (balance between freshness and performance)
- Component memoization prevents unnecessary re-renders
- CSS animations are GPU-accelerated
- Lazy loading of components

## Security Considerations

### CORS Configuration
- Explicitly configured to allow requests from localhost:3000
- Restricted to localhost (not suitable for production without adjustment)

### Input Validation
- Symbol validation (non-null, non-empty)
- Quantity and price validation (positive numbers)
- Backend validation in record compact constructor

### Data Protection
- No sensitive data stored in frontend localStorage
- All API calls use standard HTTP (development); HTTPS required for production
- Database is in-memory (data lost on restart; suitable for development)

## Testing Strategy

### Unit Testing
- Component tests for React components
- Service/Controller tests for backend
- API integration tests

### Integration Testing
- Frontend-to-Backend communication
- CORS functionality
- Full request/response cycle

### Manual Testing
- UI/UX validation
- Responsive design verification
- Cross-browser compatibility

## Deployment Considerations

### Development Deployment
- Currently runs on localhost:3000 (frontend) and localhost:8080 (backend)
- H2 in-memory database (data lost on restart)

### Production Deployment
1. **Backend:**
   - Replace H2 with persistent database (PostgreSQL, MySQL)
   - Configure Finnhub API key securely
   - Enable HTTPS
   - Update CORS to allow production domains

2. **Frontend:**
   - Build production bundle: `npm run build`
   - Deploy static files to CDN or web server
   - Update API endpoint to production backend URL
   - Enable minification and code splitting

## Future Enhancements

1. **Features:**
   - User authentication and authorization
   - Portfolio history and analytics
   - Stock recommendations
   - Price alerts and notifications
   - Export portfolio to CSV/PDF

2. **Technical:**
   - WebSocket for real-time price updates
   - Caching layer (Redis)
   - Message queue for background jobs
   - Microservices architecture
   - Containerization (Docker)

## Monitoring and Logging

### Backend Logging
- Log level: INFO (default)
- Debug logging available for `org.example.stockpotrfolio` package
- Request/response logging for API calls

### Frontend Debugging
- Browser DevTools Console
- Network tab for API monitoring
- React Developer Tools extension

## Conclusion

The Stock Portfolio Application demonstrates a modern full-stack web application with clean architecture, proper separation of concerns, and user-friendly interface. It serves as an excellent learning project for understanding Spring Boot and React integration.
