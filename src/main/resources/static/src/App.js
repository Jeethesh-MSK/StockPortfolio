import React, { useRef, useState, useEffect } from 'react';
import PortfolioList from './components/PortfolioList';
import StockPriceFetcher from './components/StockPriceFetcher';
import CandlestickChart from './components/CandlestickChart';
import './App.css';

/**
 * Main App Component
 * Serves as the entry point for the Stock Portfolio application
 */
function App() {
  // Reference to portfolio list for triggering refresh
  const portfolioListRef = useRef(null);

  // Dark mode state - check localStorage for saved preference
  const [darkMode, setDarkMode] = useState(() => {
    const saved = localStorage.getItem('darkMode');
    return saved ? JSON.parse(saved) : false;
  });

  // Apply dark mode class to body and save preference
  useEffect(() => {
    document.body.classList.toggle('dark-mode', darkMode);
    localStorage.setItem('darkMode', JSON.stringify(darkMode));
  }, [darkMode]);

  const toggleDarkMode = () => {
    setDarkMode(prev => !prev);
  };

  const handlePurchaseComplete = () => {
    // Trigger portfolio refresh when a stock is purchased
    if (portfolioListRef.current && portfolioListRef.current.refreshPortfolio) {
      portfolioListRef.current.refreshPortfolio();
    }
    // Force re-render by triggering window event
    window.dispatchEvent(new CustomEvent('portfolioUpdated'));
  };

  return (
    <div className={`app ${darkMode ? 'dark-mode' : ''}`}>
      <header className="app-header">
        <div className="header-content">
          <div className="header-left">
            <h1>📈 Stock Portfolio Manager</h1>
            <p>Manage and track your stock investments with live prices</p>
          </div>
          <div className="header-right">
            <button className="theme-toggle" onClick={toggleDarkMode} title={darkMode ? 'Switch to Light Mode' : 'Switch to Dark Mode'}>
              {darkMode ? (
                <span className="theme-icon">☀️</span>
              ) : (
                <span className="theme-icon">🌙</span>
              )}
              <span className="theme-label">{darkMode ? 'Light' : 'Dark'}</span>
            </button>
          </div>
        </div>
      </header>

      <main className="app-main">
        <div className="container">
          <div className="portfolio-section">
            <CandlestickChart />
            <PortfolioList ref={portfolioListRef} />
          </div>
          <div className="sidebar">
            <StockPriceFetcher onPurchaseComplete={handlePurchaseComplete} />
          </div>
        </div>
      </main>

      <footer className="app-footer">
        <p>Stock Portfolio Manager v1.0.0 | Powered by Spring Boot & React</p>
      </footer>
    </div>
  );
}

export default App;
