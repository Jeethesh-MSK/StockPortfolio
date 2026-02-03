# Stock Portfolio Application - Quick Reference Card

## 🚀 Startup

### Windows
```powershell
cd C:\Users\Administrator\JavaLearning\StockPotrfolio
.\startup.bat
```

### Linux/Mac
```bash
cd ~/JavaLearning/StockPotrfolio
chmod +x startup.sh
./startup.sh
```

### Manual (Two Terminals)

**Terminal 1:**
```bash
cd C:\Users\Administrator\JavaLearning\StockPotrfolio
java -jar target\StockPotrfolio-0.0.1-SNAPSHOT.jar
```

**Terminal 2:**
```bash
cd C:\Users\Administrator\JavaLearning\StockPotrfolio\src\main\resources\static
npm start
```

---

## 🌐 URLs

| Service | URL | Purpose |
|---------|-----|---------|
| Frontend | http://localhost:3000 | React portfolio app |
| Backend | http://localhost:8080 | REST API server |
| API Docs | http://localhost:8080/swagger-ui.html | Interactive API docs |
| Database | http://localhost:8080/h2-console | Database console |

---

## 📡 API Endpoints

### Portfolio
```
GET /api/portfolio
```
**Returns:** All holdings with live prices, profit/loss

### Stock Price
```
GET /api/stocks/price/{symbol}
```
**Example:** `GET /api/stocks/price/AAPL`
**Returns:** Current price and timestamp

---

## 💻 Build Commands

### Backend
```bash
# Build
mvn clean package -DskipTests

# Run tests
mvn test

# Run application
java -jar target\StockPotrfolio-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd src/main/resources/static

# Install dependencies
npm install

# Start development server
npm start

# Run tests
npm test

# Build for production
npm run build
```

---

## 📊 Project Directories

```
├── src/main/java/           → Backend (Java/Spring Boot)
├── src/main/resources/      → Configuration files
│   └── static/              → Frontend (React)
│       ├── src/components/  → React components
│       ├── src/api/         → API service
│       └── src/styles/      → CSS styling
├── target/                  → Built JAR files
└── README.md                → Main documentation
```

---

## 🧪 Testing

### Run All Tests
```bash
# Backend (from root directory)
mvn test

# Frontend (from static directory)
cd src/main/resources/static
npm test
```

### Test Categories
- **Unit Tests:** Component & service logic
- **Integration Tests:** Frontend-backend communication
- **Manual Tests:** UI/UX verification

---

## 🔧 Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven configuration |
| `StockPotrfolioApplication.java` | Spring Boot entry point |
| `PortfolioController.java` | Portfolio REST endpoints |
| `StockController.java` | Stock price endpoints |
| `src/main/resources/static/package.json` | npm configuration |
| `src/main/resources/static/src/App.js` | React main component |

---

## 📝 Feature Checklist

- ✅ View portfolio with live prices
- ✅ Calculate profit/loss
- ✅ Search stock prices
- ✅ Auto-refresh every 30 seconds
- ✅ Responsive design
- ✅ Error handling
- ✅ API documentation
- ✅ Unit tests
- ✅ Database console
- ✅ Swagger UI

---

## 🐛 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Failed to fetch portfolio" | Backend not running on port 8080 |
| Stock prices not showing | Check Finnhub API key config |
| Port 3000 in use | Change port or close other app |
| npm install fails | Delete node_modules, run again |
| Java not found | Install Java 17+ |
| React won't start | Check Node.js version |

---

## 📚 Documentation Files

- **README.md** - Full project documentation
- **TESTING.md** - Comprehensive testing guide
- **ARCHITECTURE.md** - System design & patterns
- **PROJECT_SUMMARY.md** - Overview of everything built
- **src/main/resources/static/README.md** - Frontend docs

---

## 🎯 Development Tips

### View Live API Responses
```bash
# In PowerShell/Terminal
curl http://localhost:8080/api/portfolio
curl http://localhost:8080/api/stocks/price/AAPL
```

### Debug Frontend
- Press F12 in browser
- Go to Console tab for errors
- Go to Network tab for API calls

### Debug Backend
- Check application logs in terminal
- Visit H2 console for database inspection
- Use Swagger UI to test endpoints

### Modify Database Data
- Visit http://localhost:8080/h2-console
- Run SQL: `SELECT * FROM PORTFOLIO_ITEM;`
- Insert test data as needed

---

## 📈 Portfolio Calculation Formula

```
Profit/Loss % = ((Current Price - Avg Buy Price) / Avg Buy Price) × 100

Total Value = Quantity × Current Price
Total Cost = Quantity × Avg Buy Price
Profit/Loss $ = Total Value - Total Cost
```

---

## 🔐 Security Notes

- CORS enabled for localhost:3000 only
- Requires backend API key for Finnhub (configured)
- H2 database password-less (development only)
- Input validation on all endpoints

---

## 📞 Quick Contact Points

### Frontend Issues
- Check `src/main/resources/static/README.md`
- Run `npm test` for test results
- Check browser console for errors

### Backend Issues
- Check terminal output for logs
- Visit Swagger UI for endpoint docs
- Use H2 console for database issues

### General Questions
- Read `README.md` for overview
- Check `ARCHITECTURE.md` for design
- See `TESTING.md` for testing info

---

## ⏱️ Typical Workflow

1. **Start Application**
   ```powershell
   .\startup.bat
   ```

2. **Open in Browser**
   ```
   http://localhost:3000
   ```

3. **View Portfolio**
   - Data loads automatically
   - Updates every 30 seconds

4. **Search Stock Prices**
   - Enter symbol in sidebar
   - See current price

5. **Check API Docs**
   ```
   http://localhost:8080/swagger-ui.html
   ```

6. **Access Database**
   ```
   http://localhost:8080/h2-console
   ```

---

**Version:** 1.0.0  
**Last Updated:** February 2, 2026  
**Status:** ✅ Ready to Use
