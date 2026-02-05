import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { buyStock, sellStock } from '../api/portfolioAPI';
import '../styles/PortfolioItem.css';

/**
 * PortfolioItem Component
 * Displays a single stock holding with its details and performance metrics
 * Includes buy and sell buttons for transaction management
 */
const PortfolioItem = ({ symbol, quantity, averageBuyPrice, currentLivePrice, profitOrLossPercentage, onTransactionComplete }) => {
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState(null); // 'buy' or 'sell'
  const [transactionQuantity, setTransactionQuantity] = useState('');
  const [transactionPrice, setTransactionPrice] = useState(currentLivePrice);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  const totalCost = quantity * averageBuyPrice;
  const totalValue = quantity * currentLivePrice;
  const profitOrLossDollar = totalValue - totalCost;
  const isProfit = profitOrLossDollar >= 0;

  const openModal = (type) => {
    setModalType(type);
    setTransactionQuantity('');
    setTransactionPrice(currentLivePrice);
    setError(null);
    setSuccessMessage(null);
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setModalType(null);
    setError(null);
    setSuccessMessage(null);
  };

  const handleTransaction = async () => {
    setError(null);
    setSuccessMessage(null);

    // Validate input
    if (!transactionQuantity || transactionQuantity <= 0) {
      setError('Please enter a valid quantity');
      return;
    }

    const qty = parseInt(transactionQuantity);

    if (modalType === 'sell' && qty > quantity) {
      setError(`Cannot sell ${qty} shares. You only have ${quantity} shares.`);
      return;
    }

    if (modalType === 'buy' && (!transactionPrice || transactionPrice <= 0)) {
      setError('Please enter a valid price');
      return;
    }

    setLoading(true);

    try {
      if (modalType === 'buy') {
        await buyStock(symbol, qty, parseFloat(transactionPrice));
        setSuccessMessage(`Successfully bought ${qty} shares of ${symbol}!`);
      } else {
        await sellStock(symbol, qty);
        setSuccessMessage(`Successfully sold ${qty} shares of ${symbol}!`);
      }

      // Trigger parent component to refresh portfolio
      if (onTransactionComplete) {
        setTimeout(onTransactionComplete, 1500);
      }

      // Close modal after a brief delay to show success message
      setTimeout(closeModal, 2000);
    } catch (err) {
      setError(err.response?.data?.message || `Error processing ${modalType}: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="portfolio-item">
      <div className="portfolio-item-header">
        <h3 className="symbol">{symbol}</h3>
        <div className={`performance-badge ${isProfit ? 'profit' : 'loss'}`}>
          <span className="perf-arrow">{isProfit ? '↑' : '↓'}</span> {profitOrLossPercentage.toFixed(2)}%
        </div>
      </div>

      <div className="portfolio-item-details">
        <div className="detail-row">
          <span className="label">Quantity:</span>
          <span className="value">{quantity} shares</span>
        </div>

        <div className="detail-row">
          <span className="label">Avg Buy Price:</span>
          <span className="value">${averageBuyPrice.toFixed(2)}</span>
        </div>

        <div className="detail-row">
          <span className="label">Current Price:</span>
          <span className="value">${currentLivePrice.toFixed(2)}</span>
        </div>

        <div className="detail-row">
          <span className="label">Total Cost:</span>
          <span className="value">${totalCost.toFixed(2)}</span>
        </div>

        <div className="detail-row">
          <span className="label">Current Value:</span>
          <span className="value">${totalValue.toFixed(2)}</span>
        </div>

        <div className={`detail-row profit-loss ${isProfit ? 'positive' : 'negative'}`}>
          <span className="label">Profit/Loss:</span>
          <span className="value">${profitOrLossDollar.toFixed(2)}</span>
        </div>
      </div>

      <div className="portfolio-item-actions">
        <button className="btn-buy" onClick={() => openModal('buy')}>
          Buy
        </button>
        <button className="btn-sell" onClick={() => openModal('sell')}>
          Sell
        </button>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{modalType === 'buy' ? 'Buy' : 'Sell'} {symbol}</h2>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>

            {error && <div className="modal-error">{error}</div>}
            {successMessage && <div className="modal-success">{successMessage}</div>}

            {!successMessage && (
              <div className="modal-body">
                <div className="form-group">
                  <label>Quantity (shares)</label>
                  <input
                    type="number"
                    value={transactionQuantity}
                    onChange={(e) => setTransactionQuantity(e.target.value)}
                    placeholder="Enter quantity"
                    min="1"
                    max={modalType === 'sell' ? quantity : undefined}
                    disabled={loading}
                  />
                </div>

                {modalType === 'buy' && (
                  <div className="form-group">
                    <label>Price per share ($)</label>
                    <input
                      type="number"
                      value={transactionPrice}
                      onChange={(e) => setTransactionPrice(e.target.value)}
                      placeholder="Enter price"
                      step="0.01"
                      min="0.01"
                      disabled={loading}
                    />
                  </div>
                )}

                {transactionQuantity && (
                  <div className="transaction-summary">
                    <div className="summary-item">
                      <span>Quantity:</span>
                      <strong>{transactionQuantity} shares</strong>
                    </div>
                    {modalType === 'buy' && (
                      <div className="summary-item">
                        <span>Price per share:</span>
                        <strong>${parseFloat(transactionPrice || 0).toFixed(2)}</strong>
                      </div>
                    )}
                    <div className="summary-item total">
                      <span>Total {modalType === 'buy' ? 'Cost' : 'Proceeds'}:</span>
                      <strong>${(parseInt(transactionQuantity) * (modalType === 'buy' ? parseFloat(transactionPrice || 0) : currentLivePrice)).toFixed(2)}</strong>
                    </div>
                  </div>
                )}

                <div className="modal-actions">
                  <button
                    className="btn-cancel"
                    onClick={closeModal}
                    disabled={loading}
                  >
                    Cancel
                  </button>
                  <button
                    className={`btn-confirm ${modalType === 'buy' ? 'btn-confirm-buy' : 'btn-confirm-sell'}`}
                    onClick={handleTransaction}
                    disabled={loading || !transactionQuantity}
                  >
                    {loading ? 'Processing...' : (modalType === 'buy' ? 'Buy' : 'Sell')}
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

PortfolioItem.propTypes = {
  symbol: PropTypes.string.isRequired,
  quantity: PropTypes.number.isRequired,
  averageBuyPrice: PropTypes.number.isRequired,
  currentLivePrice: PropTypes.number.isRequired,
  profitOrLossPercentage: PropTypes.number.isRequired,
  onTransactionComplete: PropTypes.func,
};

export default PortfolioItem;
