import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * Fetch the complete portfolio with live prices
 * @returns {Promise} Promise with portfolio data
 */
export const getPortfolio = async () => {
  try {
    const response = await axiosInstance.get('/portfolio');
    return response.data;
  } catch (error) {
    console.error('Error fetching portfolio:', error);
    throw error;
  }
};

/**
 * Fetch the current price for a specific stock symbol
 * @param {string} symbol - Stock symbol (e.g., 'AAPL', 'GOOGL')
 * @returns {Promise} Promise with stock price data
 */
export const getStockPrice = async (symbol) => {
  try {
    const response = await axiosInstance.get(`/stocks/price/${symbol}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching price for ${symbol}:`, error);
    throw error;
  }
};

/**
 * Buy stocks and add them to the portfolio
 * @param {string} symbol - Stock symbol (e.g., 'AAPL')
 * @param {number} quantity - Number of shares to buy
 * @param {number} price - Price per share
 * @returns {Promise} Promise with transaction result
 */
export const buyStock = async (symbol, quantity, price) => {
  try {
    const response = await axiosInstance.post('/portfolio/buy', {
      symbol: symbol,
      quantity: quantity,
      price: price
    });
    return response.data;
  } catch (error) {
    console.error(`Error buying stock ${symbol}:`, error);
    throw error;
  }
};

/**
 * Sell stocks from the portfolio
 * @param {string} symbol - Stock symbol (e.g., 'AAPL')
 * @param {number} quantity - Number of shares to sell
 * @returns {Promise} Promise with transaction result
 */
export const sellStock = async (symbol, quantity) => {
  try {
    const response = await axiosInstance.post('/portfolio/sell', {
      symbol: symbol,
      quantity: quantity
    });
    return response.data;
  } catch (error) {
    console.error(`Error selling stock ${symbol}:`, error);
    throw error;
  }
};

export default axiosInstance;
