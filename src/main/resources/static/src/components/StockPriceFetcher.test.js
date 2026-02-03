import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';
import StockPriceFetcher from '../components/StockPriceFetcher';

jest.mock('axios');

describe('StockPriceFetcher Component', () => {
  const mockStockData = {
    symbol: 'AAPL',
    price: 165.0,
    timestamp: '2024-02-02T10:30:00Z'
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders stock price fetcher component', () => {
    render(<StockPriceFetcher />);
    expect(screen.getByText('Stock Price Lookup')).toBeInTheDocument();
  });

  test('renders search input field', () => {
    render(<StockPriceFetcher />);
    expect(screen.getByPlaceholderText(/Enter stock symbol/)).toBeInTheDocument();
  });

  test('renders search button', () => {
    render(<StockPriceFetcher />);
    expect(screen.getByText('Search')).toBeInTheDocument();
  });

  test('shows error when symbol is empty', async () => {
    render(<StockPriceFetcher />);
    const searchBtn = screen.getByText('Search');
    fireEvent.click(searchBtn);

    await waitFor(() => {
      expect(screen.getByText(/Please enter a stock symbol/)).toBeInTheDocument();
    });
  });

  test('displays stock price after successful search', async () => {
    axios.get.mockResolvedValue({ data: mockStockData });
    render(<StockPriceFetcher />);

    const input = screen.getByPlaceholderText(/Enter stock symbol/);
    fireEvent.change(input, { target: { value: 'AAPL' } });

    const searchBtn = screen.getByText('Search');
    fireEvent.click(searchBtn);

    await waitFor(() => {
      expect(screen.getByText('AAPL')).toBeInTheDocument();
      expect(screen.getByText('$165.00')).toBeInTheDocument();
    });
  });

  test('converts symbol to uppercase', async () => {
    axios.get.mockResolvedValue({ data: mockStockData });
    render(<StockPriceFetcher />);

    const input = screen.getByPlaceholderText(/Enter stock symbol/);
    fireEvent.change(input, { target: { value: 'aapl' } });

    const searchBtn = screen.getByText('Search');
    fireEvent.click(searchBtn);

    await waitFor(() => {
      expect(axios.get).toHaveBeenCalledWith('/stocks/price/AAPL');
    });
  });

  test('handles API error gracefully', async () => {
    axios.get.mockRejectedValue(new Error('API Error'));
    render(<StockPriceFetcher />);

    const input = screen.getByPlaceholderText(/Enter stock symbol/);
    fireEvent.change(input, { target: { value: 'INVALID' } });

    const searchBtn = screen.getByText('Search');
    fireEvent.click(searchBtn);

    await waitFor(() => {
      expect(screen.getByText(/Failed to fetch price/)).toBeInTheDocument();
    });
  });

  test('displays timestamp when available', async () => {
    axios.get.mockResolvedValue({ data: mockStockData });
    render(<StockPriceFetcher />);

    const input = screen.getByPlaceholderText(/Enter stock symbol/);
    fireEvent.change(input, { target: { value: 'AAPL' } });

    const searchBtn = screen.getByText('Search');
    fireEvent.click(searchBtn);

    await waitFor(() => {
      expect(screen.getByText(/Last Updated/)).toBeInTheDocument();
    });
  });

  test('disables input while searching', async () => {
    axios.get.mockImplementation(() => new Promise(() => {}));
    render(<StockPriceFetcher />);

    const input = screen.getByPlaceholderText(/Enter stock symbol/);
    fireEvent.change(input, { target: { value: 'AAPL' } });

    const searchBtn = screen.getByText('Search');
    fireEvent.click(searchBtn);

    expect(input).toBeDisabled();
    expect(searchBtn).toBeDisabled();
  });
});
