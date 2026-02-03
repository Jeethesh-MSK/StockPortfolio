import React, { useState } from 'react';
import { getStockPrice, buyStock } from '../api/portfolioAPI';
import '../styles/StockPriceFetcher.css';

/**
 * StockPriceFetcher Component
 * Allows users to search for stock prices and buy stocks
 */
const StockPriceFetcher = ({ onPurchaseComplete }) => {
  const [symbol, setSymbol] = useState('');
  const [stockData, setStockData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Buy modal state
  const [showBuyModal, setShowBuyModal] = useState(false);
  const [buyQuantity, setBuyQuantity] = useState('');
  const [buyPrice, setBuyPrice] = useState('');
  const [buyLoading, setBuyLoading] = useState(false);
  const [buyError, setBuyError] = useState(null);
  const [buySuccess, setBuySuccess] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();

    if (!symbol.trim()) {
      setError('Please enter a stock symbol');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await getStockPrice(symbol.toUpperCase());
      setStockData(data);
    } catch (err) {
      setError(`Failed to fetch price for ${symbol}. Check the symbol and try again.`);
      setStockData(null);
    } finally {
      setLoading(false);
    }
  };

  const openBuyModal = () => {
    setBuyQuantity('');
    setBuyPrice(stockData?.price?.toFixed(2) || '');
    setBuyError(null);
    setBuySuccess(null);
    setShowBuyModal(true);
  };

  const closeBuyModal = () => {
    setShowBuyModal(false);
    setBuyError(null);
    setBuySuccess(null);
  };

  const handleBuyStock = async () => {
    setBuyError(null);
    setBuySuccess(null);

    if (!buyQuantity || parseInt(buyQuantity) <= 0) {
      setBuyError('Please enter a valid quantity');
      return;
    }

    if (!buyPrice || parseFloat(buyPrice) <= 0) {
      setBuyError('Please enter a valid price');
      return;
    }

    setBuyLoading(true);

    try {
      const stockSymbol = stockData?.symbol || symbol.toUpperCase();
      await buyStock(stockSymbol, parseInt(buyQuantity), parseFloat(buyPrice));
      setBuySuccess(`Successfully bought ${buyQuantity} shares of ${stockSymbol}!`);

      // Trigger parent to refresh portfolio if callback provided
      if (onPurchaseComplete) {
        setTimeout(onPurchaseComplete, 1500);
      }

      // Close modal after showing success
      setTimeout(() => {
        closeBuyModal();
        // Clear the search to encourage viewing portfolio
        setStockData(null);
        setSymbol('');
      }, 2000);
    } catch (err) {
      setBuyError(err.response?.data?.message || `Error buying stock: ${err.message}`);
    } finally {
      setBuyLoading(false);
    }
  };

  return (
    <div className="stock-price-fetcher">
      <h2>Stock Price Lookup</h2>

      <form onSubmit={handleSearch} className="search-form">
        <input
          type="text"
          placeholder="Enter stock symbol (e.g., AAPL, GOOGL, MSFT)"
          value={symbol}
          onChange={(e) => setSymbol(e.target.value)}
          className="symbol-input"
          disabled={loading}
        />
        <button type="submit" disabled={loading} className="search-btn">
          {loading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && (
        <div className="error-message">
          <span>⚠️ {error}</span>
        </div>
      )}

      {stockData && (
        <div className="stock-result">
          <div className="result-header">
            <h3>{stockData.symbol || symbol.toUpperCase()}</h3>
          </div>
          <div className="result-details">
            <div className="result-row">
              <span className="label">Current Price:</span>
              <span className="value">${stockData.price?.toFixed(2) || 'N/A'}</span>
            </div>
            {stockData.timestamp && (
              <div className="result-row">
                <span className="label">Last Updated:</span>
                <span className="value">{new Date(stockData.timestamp).toLocaleString()}</span>
              </div>
            )}
          </div>

          {/* Buy Button */}
          <div className="stock-actions">
            <button className="buy-stock-btn" onClick={openBuyModal}>
              💰 Buy This Stock
            </button>
          </div>
        </div>
      )}

      {/* Buy Modal */}
      {showBuyModal && (
        <div className="modal-overlay" onClick={closeBuyModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Buy {stockData?.symbol || symbol.toUpperCase()}</h2>
              <button className="modal-close" onClick={closeBuyModal}>×</button>
            </div>

            {buyError && <div className="modal-error">{buyError}</div>}
            {buySuccess && <div className="modal-success">{buySuccess}</div>}

            {!buySuccess && (
              <div className="modal-body">
                <div className="current-price-info">
                  <span>Current Market Price:</span>
                  <strong>${stockData?.price?.toFixed(2) || 'N/A'}</strong>
                </div>

                <div className="form-group">
                  <label>Quantity (shares)</label>
                  <input
                    type="number"
                    value={buyQuantity}
                    onChange={(e) => setBuyQuantity(e.target.value)}
                    placeholder="Enter quantity"
                    min="1"
                    disabled={buyLoading}
                  />
                </div>

                <div className="form-group">
                  <label>Price per share ($)</label>
                  <input
                    type="number"
                    value={buyPrice}
                    onChange={(e) => setBuyPrice(e.target.value)}
                    placeholder="Enter price"
                    step="0.01"
                    min="0.01"
                    disabled={buyLoading}
                  />
                </div>

                {buyQuantity && buyPrice && (
                  <div className="transaction-summary">
                    <div className="summary-item">
                      <span>Quantity:</span>
                      <strong>{buyQuantity} shares</strong>
                    </div>
                    <div className="summary-item">
                      <span>Price per share:</span>
                      <strong>${parseFloat(buyPrice).toFixed(2)}</strong>
                    </div>
                    <div className="summary-item total">
                      <span>Total Cost:</span>
                      <strong>${(parseInt(buyQuantity) * parseFloat(buyPrice)).toFixed(2)}</strong>
                    </div>
                  </div>
                )}

                <div className="modal-actions">
                  <button className="btn-cancel" onClick={closeBuyModal} disabled={buyLoading}>
                    Cancel
                  </button>
                  <button className="btn-confirm buy" onClick={handleBuyStock} disabled={buyLoading}>
                    {buyLoading ? 'Processing...' : 'Confirm Purchase'}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default StockPriceFetcher;
