# 📈 Stock Portfolio Manager

A full-stack web application for managing and tracking stock investments with real-time pricing and portfolio analysis.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Backend Setup](#backend-setup)
- [Frontend Setup](#frontend-setup)
- [Testing](#testing)
- [API Endpoints](#api-endpoints)
- [Architecture](#architecture)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## 🎯 Overview

Stock Portfolio Manager is a full-stack web application built with Spring Boot and React. It demonstrates modern software development practices including:
- RESTful API design
- Component-based UI architecture
- Responsive design
- Real-time data updates
- Comprehensive testing
- Clean code principles

The application connects to the Finnhub API to fetch live stock prices and calculates portfolio metrics in real-time.

StockPortfolio is a Java Spring Boot application designed to help users manage their stock investments efficiently. It provides real-time stock price tracking, portfolio management with weighted average price calculations, and comprehensive profit/loss analysis.

**Status:** ✅ **Production Ready** - All features tested and working

---

## ✨ Features

### 1. **Real-Time Stock Price Fetching**
   - Integrates with Finnhub API to fetch live stock prices
   - Supports all stock symbols available on Finnhub
   - Automatic price updates on-demand
   - Graceful error handling with fallback mechanisms
   - Comprehensive logging for debugging

### 2. **Portfolio Management System**
   - Create and manage portfolio items
   - Add stocks to portfolio with purchase price and quantity
   - Automatic weighted average price calculation when buying more of the same stock
   - Formula: `NewPrice = (OldQty × OldPrice + NewQty × BuyPrice) / (OldQty + NewQty)`

### 3. **Unified Portfolio View**
   - View complete portfolio with live market prices
   - Each holding shows:
     - Stock symbol
     - Quantity owned
     - Average buy price
     - Current live price
     - Profit/loss percentage
     - Total invested amount
     - Current market value
     - Absolute profit/loss in dollars
   - Aggregate portfolio metrics:
     - Total items count
     - Total invested amount
     - Current total portfolio value
     - Total gain/loss in dollars
     - Overall portfolio gain/loss percentage

### 4. **Profit & Loss Calculations**
   - Real-time profit/loss percentage: `(CurrentPrice - AvgPrice) / AvgPrice × 100`
   - Absolute profit/loss calculation: `CurrentValue - InvestedAmount`
   - Profit indicator for quick visual identification
   - Formatted display for currency and percentages

### 5. **Interactive API Documentation**
   - Swagger UI for testing endpoints
   - OpenAPI specification
   - Comprehensive endpoint documentation
   - Example request/response bodies

### 6. **Database Persistence**
   - H2 in-memory database for testing
   - JPA/Hibernate ORM mapping
   - Automatic table creation
   - Support for scaling to production databases

### 7. **Error Handling & Logging**
   - Comprehensive exception handling
   - Detailed logging with SLF4J
   - Graceful degradation when API calls fail
   - Input validation on all endpoints

---

## 🏗️ Architecture

### **Layered Architecture**

```
┌─────────────────────────────────────────┐
│         REST Controllers                 │
│  (StockController, PortfolioController) │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│           Services                      │
│ (StockPriceService, PortfolioService)  │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│        Data Access Layer                │
│    (PortfolioRepository)                │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│       JPA/Hibernate & H2 Database       │
└─────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│    External APIs (Finnhub API)          │
└─────────────────────────────────────────┘
```

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 4.0.2 |
| **Java Version** | Java | 17+ |
| **ORM** | Hibernate/JPA | 7.2.1 |
| **Database** | H2 | 2.4.240 |
| **Web Server** | Tomcat | 11.0.15 |
| **Build Tool** | Maven | 3.x |
| **Utility** | Lombok | Latest |
| **API Docs** | SpringDoc OpenAPI | 2.6.0 |
| **Logging** | SLF4J** | Latest |
| **HTTP Client** | RestTemplate | Spring Native |

---

## 📁 Project Structure

```
StockPortfolio/
├── src/
│   ├── main/
│   │   ├── java/org/example/stockpotrfolio/
│   │   │   ├── StockPotrfolioApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── StockController.java          # Stock price endpoints
│   │   │   │   └── PortfolioController.java      # Portfolio endpoints
│   │   │   ├── service/
│   │   │   │   ├── StockPriceService.java        # Finnhub API integration
│   │   │   │   └── PortfolioService.java         # Portfolio business logic
│   │   │   ├── repository/
│   │   │   │   └── PortfolioRepository.java      # JPA repository
│   │   │   ├── entity/
│   │   │   │   └── PortfolioItem.java            # JPA entity
│   │   │   └── dto/
│   │   │       └── PortfolioSummary.java         # Data transfer object
│   │   └── resources/
│   │       └── application.properties             # Configuration
│   └── test/
│       └── java/org/example/stockpotrfolio/
│           └── StockPotrfolioApplicationTests.java
├── pom.xml                                        # Maven dependencies
├── mvnw & mvnw.cmd                               # Maven wrapper
└── README.md

```

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for Finnhub API)

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd StockPotrfolio
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   ```bash
   java -jar target/StockPotrfolio-0.0.1-SNAPSHOT.jar
   ```

   Or using Maven:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/v3/api-docs
   - H2 Console: http://localhost:8080/h2-console

---

## 📡 API Endpoints

### **Stock Controller** (`/api/stocks`)

#### Get Stock Price
```http
GET /api/stocks/price/{symbol}
```
**Parameters:**
- `symbol` (path): Stock symbol (e.g., AAPL, GOOGL)

**Response (200 OK):**
```json
{
  "symbol": "AAPL",
  "price": 264.59
}
```

**Error Response (400/404/500):**
```json
{
  "error": "Error type",
  "message": "Detailed error message"
}
```

---

### **Portfolio Controller** (`/api/portfolio`)

#### Get Complete Portfolio with Live Prices
```http
GET /api/portfolio
```

**Response (200 OK):**
```json
{
  "portfolio": [
    {
      "symbol": "AAPL",
      "quantity": 10,
      "averageBuyPrice": 150.0,
      "currentLivePrice": 264.59,
      "profitOrLossPercentage": 76.39
    },
    {
      "symbol": "GOOGL",
      "quantity": 5,
      "averageBuyPrice": 100.0,
      "currentLivePrice": 140.0,
      "profitOrLossPercentage": 40.0
    }
  ],
  "totalItems": 2,
  "totalInvested": 2000.0,
  "currentTotalValue": 3023.0,
  "totalGainLoss": 1023.0,
  "totalGainLossPercentage": 51.15
}
```

---

## 💾 Database

### H2 In-Memory Database Configuration

**JDBC URL:** `jdbc:h2:mem:testdb`
**Username:** `sa`
**Password:** (empty)

### Tables

#### `portfolio_items`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key (auto-generated) |
| symbol | VARCHAR | Stock symbol |
| average_buy_price | DOUBLE | Average purchase price |
| quantity | INTEGER | Number of shares |

---

## ⚙️ Configuration

### Application Properties (`application.properties`)

```properties
# Server
server.port=8080

# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Swagger/OpenAPI
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.enabled=true
```

---

## 🔑 Key Classes & Components

### **1. StockPriceService**
- **Responsibility:** Fetches real-time stock prices from Finnhub API
- **Key Methods:**
  - `getCurrentPrice(String symbol)` - Returns price or null
  - `getCurrentPriceOrThrow(String symbol)` - Returns price or throws exception
- **Features:** Error handling, input validation, comprehensive logging

### **2. PortfolioService**
- **Responsibility:** Manages portfolio operations and calculations
- **Key Methods:**
  - `buyStock(String symbol, Double buyPrice, Integer quantity)` - Add/update stocks
  - `getPortfolio()` - Retrieve all portfolio items
  - `getPortfolioItemBySymbol(String symbol)` - Get specific holding
  - `getPortfolioSummary(Map currentPrices)` - Get portfolio with live prices
  - `getTotalPortfolioGainLoss(Map currentPrices)` - Calculate total P&L
  - `getWeightedAverageGainLossPercentage(Map currentPrices)` - Portfolio return

### **3. PortfolioSummary (DTO)**
- **Responsibility:** Combines database and live price data
- **Fields:** symbol, quantity, averageBuyPrice, currentLivePrice, profitOrLossPercentage
- **Features:** Immutable record, validation, utility methods for calculations

### **4. PortfolioItem (Entity)**
- **Responsibility:** JPA entity representing portfolio holdings
- **Fields:** id, symbol, averageBuyPrice, quantity
- **Annotations:** @Entity, @Table, Lombok @Data

### **5. Controllers**
- **StockController:** Handles stock price queries
- **PortfolioController:** Handles portfolio viewing and management

---

## 📊 Example Workflows

### Scenario 1: Get Current Stock Price
```
User → GET /api/stocks/price/AAPL
  ↓
StockController → StockPriceService.getCurrentPrice("AAPL")
  ↓
Finnhub API → {"c": 264.59, ...}
  ↓
Response → {"symbol": "AAPL", "price": 264.59}
```

### Scenario 2: View Complete Portfolio
```
User → GET /api/portfolio
  ↓
PortfolioController → PortfolioService.getPortfolio()
  ↓
Database Query → [PortfolioItem(AAPL, 10, 150.0), ...]
  ↓
For each item → StockPriceService.getCurrentPrice(symbol)
  ↓
Finnhub API → Live prices
  ↓
PortfolioSummary.from() → Calculate P&L
  ↓
Response → Complete portfolio with metrics
```

### Scenario 3: Buy Stock (Update Average Price)
```
User → buyStock("AAPL", 170.0, 5)
  ↓
PortfolioService.buyStock()
  ↓
Check if AAPL exists:
  → If YES: updateExistingStock()
      Weighted Average = (10 × 150 + 5 × 170) / (10 + 5) = 156.67
  → If NO: createNewStock()
  ↓
Save to Database
  ↓
Return PortfolioItem
```

---

## 🧪 Testing

The application has been tested and verified:
- ✅ Swagger UI accessible
- ✅ API documentation available
- ✅ Stock price endpoint functional
- ✅ Portfolio endpoint functional
- ✅ H2 database operational
- ✅ Live price fetching from Finnhub API working
- ✅ Error handling implemented

---

## 📝 Logging

The application uses **SLF4J** for comprehensive logging:
- **INFO Level:** Major operations (API calls, portfolio actions)
- **DEBUG Level:** Detailed information (price calculations, data processing)
- **WARN Level:** Warning conditions (missing data, invalid inputs)
- **ERROR Level:** Error conditions (API failures, exceptions)

---

## 🔐 Security & Validation

- Input validation on all endpoints
- Null/empty string checks
- Positive number validation
- Exception handling with proper error responses
- Transaction management for data consistency

---

## 🚧 Future Enhancements

- [ ] User authentication & authorization
- [ ] Multiple user portfolios
- [ ] Buy/Sell transaction history
- [ ] Portfolio performance analytics
- [ ] Dividend tracking
- [ ] Tax lot tracking
- [ ] Watchlist functionality
- [ ] Price alerts
- [ ] Database migration to PostgreSQL/MySQL
- [ ] Caching for API responses

---

## 📄 License

This project is provided as-is for educational purposes.

---

## 👨‍💻 Development

Built with ❤️ using Spring Boot 4.0.2 and Java 17

For questions or issues, please refer to the code documentation and API documentation at http://localhost:8080/swagger-ui.html
