import React, { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import { getPortfolio } from '../api/portfolioAPI';
import PortfolioItem from './PortfolioItem';
import '../styles/PortfolioList.css';

/**
 * PortfolioList Component
 * Fetches and displays all portfolio items with live prices
 */
const PortfolioList = forwardRef((props, ref) => {
  const [portfolioItems, setPortfolioItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  // Expose refreshPortfolio method to parent components
  useImperativeHandle(ref, () => ({
    refreshPortfolio: fetchPortfolio
  }));

  useEffect(() => {
    fetchPortfolio();
    // Set up auto-refresh every 30 seconds
    const interval = setInterval(fetchPortfolio, 30000);

    // Listen for portfolio update events
    const handlePortfolioUpdate = () => fetchPortfolio();
    window.addEventListener('portfolioUpdated', handlePortfolioUpdate);

    return () => {
      clearInterval(interval);
      window.removeEventListener('portfolioUpdated', handlePortfolioUpdate);
    };
  }, []);

  const fetchPortfolio = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getPortfolio();
      setPortfolioItems(data.portfolio || []);
      setLastUpdated(new Date());
    } catch (err) {
      setError('Failed to fetch portfolio. Make sure the backend is running on http://localhost:8080');
      console.error('API Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const calculateTotalValue = () => {
    return portfolioItems.reduce((total, item) => {
      return total + (item.quantity * item.currentLivePrice);
    }, 0);
  };

  const calculateTotalCost = () => {
    return portfolioItems.reduce((total, item) => {
      return total + (item.quantity * item.averageBuyPrice);
    }, 0);
  };

  const calculateOverallReturn = () => {
    const totalValue = calculateTotalValue();
    const totalCost = calculateTotalCost();
    if (totalCost === 0) return 0;
    return ((totalValue - totalCost) / totalCost) * 100;
  };

  if (loading && portfolioItems.length === 0) {
    return (
      <div className="portfolio-list loading">
        <div className="spinner"></div>
        <p>Loading portfolio...</p>
      </div>
    );
  }

  const totalValue = calculateTotalValue();
  const totalCost = calculateTotalCost();
  const totalProfit = totalValue - totalCost;
  const overallReturn = calculateOverallReturn();
  const isPositive = totalProfit >= 0;

  // SVG Icons
  const RefreshIcon = () => (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
      <path d="M3 3v5h5"/>
      <path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16"/>
      <path d="M16 21h5v-5"/>
    </svg>
  );

  const AlertIcon = () => (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10"/>
      <line x1="12" y1="8" x2="12" y2="12"/>
      <line x1="12" y1="16" x2="12.01" y2="16"/>
    </svg>
  );

  return (
    <div className="portfolio-list">
      <div className="portfolio-header">
        <h2>Stock Portfolio</h2>
        <button onClick={fetchPortfolio} disabled={loading} className="refresh-btn" title="Refresh portfolio">
          <RefreshIcon />
          <span>{loading ? 'Refreshing...' : 'Refresh'}</span>
        </button>
      </div>

      {error && (
        <div className="error-message">
          <AlertIcon />
          <span>{error}</span>
        </div>
      )}

      {lastUpdated && (
        <div className="last-updated">
          Last updated: {lastUpdated.toLocaleTimeString()}
        </div>
      )}

      <div className="portfolio-summary">
        <div className="summary-card">
          <h3>Total Portfolio Value</h3>
          <p className="value">${totalValue.toFixed(2)}</p>
        </div>
        <div className="summary-card">
          <h3>Total Investment</h3>
          <p className="value">${totalCost.toFixed(2)}</p>
        </div>
        <div className={`summary-card ${isPositive ? 'profit' : 'loss'}`}>
          <h3>Total Gain/Loss</h3>
          <p className="value">${totalProfit.toFixed(2)}</p>
          <p className="percentage">{overallReturn.toFixed(2)}%</p>
        </div>
      </div>

      {portfolioItems.length === 0 ? (
        <div className="no-items">
          <p>No portfolio items found. Add stocks to your portfolio to get started!</p>
        </div>
      ) : (
        <div className="items-container">
          {portfolioItems.map((item, index) => (
            <PortfolioItem
              key={`${item.symbol}-${index}`}
              {...item}
              onTransactionComplete={fetchPortfolio}
            />
          ))}
        </div>
      )}
    </div>
  );
});

export default PortfolioList;
