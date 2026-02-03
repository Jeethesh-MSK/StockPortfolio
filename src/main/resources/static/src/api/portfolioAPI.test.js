import { getPortfolio, getStockPrice } from '../api/portfolioAPI';
import axios from 'axios';

jest.mock('axios');

describe('Portfolio API', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getPortfolio', () => {
    test('fetches portfolio data successfully', async () => {
      const mockData = {
        portfolio: [
          {
            symbol: 'AAPL',
            quantity: 10,
            averageBuyPrice: 150.0,
            currentLivePrice: 165.0,
            profitOrLossPercentage: 10.0
          }
        ]
      };

      axios.get.mockResolvedValue({ data: mockData });

      const result = await getPortfolio();
      expect(result).toEqual(mockData);
      expect(axios.get).toHaveBeenCalledWith('/portfolio');
    });

    test('handles API errors', async () => {
      const errorMessage = 'Network error';
      axios.get.mockRejectedValue(new Error(errorMessage));

      try {
        await getPortfolio();
      } catch (error) {
        expect(error.message).toEqual(errorMessage);
      }
    });
  });

  describe('getStockPrice', () => {
    test('fetches stock price successfully', async () => {
      const mockData = {
        symbol: 'AAPL',
        price: 165.0,
        timestamp: '2024-02-02T10:30:00Z'
      };

      axios.get.mockResolvedValue({ data: mockData });

      const result = await getStockPrice('AAPL');
      expect(result).toEqual(mockData);
      expect(axios.get).toHaveBeenCalledWith('/stocks/price/AAPL');
    });

    test('handles API errors for invalid symbol', async () => {
      const errorMessage = 'Symbol not found';
      axios.get.mockRejectedValue(new Error(errorMessage));

      try {
        await getStockPrice('INVALID');
      } catch (error) {
        expect(error.message).toEqual(errorMessage);
      }
    });
  });
});
