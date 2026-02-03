import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';
import PortfolioList from '../components/PortfolioList';

jest.mock('axios');

describe('PortfolioList Component', () => {
  const mockPortfolioData = {
    portfolio: [
      {
        symbol: 'AAPL',
        quantity: 10,
        averageBuyPrice: 150.0,
        currentLivePrice: 165.0,
        profitOrLossPercentage: 10.0
      },
      {
        symbol: 'GOOGL',
        quantity: 5,
        averageBuyPrice: 2800.0,
        currentLivePrice: 2900.0,
        profitOrLossPercentage: 3.57
      }
    ]
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders portfolio list with header', () => {
    axios.get.mockResolvedValue({ data: mockPortfolioData });
    render(<PortfolioList />);
    expect(screen.getByText('Stock Portfolio')).toBeInTheDocument();
  });

  test('displays refresh button', () => {
    axios.get.mockResolvedValue({ data: mockPortfolioData });
    render(<PortfolioList />);
    expect(screen.getByText('Refresh')).toBeInTheDocument();
  });

  test('displays loading state initially', async () => {
    axios.get.mockImplementation(() => new Promise(() => {}));
    render(<PortfolioList />);
    expect(screen.getByText('Loading portfolio...')).toBeInTheDocument();
  });

  test('displays portfolio items after loading', async () => {
    axios.get.mockResolvedValue({ data: mockPortfolioData });
    render(<PortfolioList />);

    await waitFor(() => {
      expect(screen.getByText('AAPL')).toBeInTheDocument();
      expect(screen.getByText('GOOGL')).toBeInTheDocument();
    });
  });

  test('displays portfolio summary cards', async () => {
    axios.get.mockResolvedValue({ data: mockPortfolioData });
    render(<PortfolioList />);

    await waitFor(() => {
      expect(screen.getByText('Total Portfolio Value')).toBeInTheDocument();
      expect(screen.getByText('Total Investment')).toBeInTheDocument();
      expect(screen.getByText('Total Gain/Loss')).toBeInTheDocument();
    });
  });

  test('calculates total portfolio value correctly', async () => {
    axios.get.mockResolvedValue({ data: mockPortfolioData });
    render(<PortfolioList />);

    await waitFor(() => {
      const totalValue = (10 * 165.0) + (5 * 2900.0);
      expect(screen.getByText(`$${totalValue.toFixed(2)}`)).toBeInTheDocument();
    });
  });

  test('handles API error gracefully', async () => {
    axios.get.mockRejectedValue(new Error('API Error'));
    render(<PortfolioList />);

    await waitFor(() => {
      expect(screen.getByText(/Failed to fetch portfolio/)).toBeInTheDocument();
    });
  });

  test('displays "no items" message when portfolio is empty', async () => {
    axios.get.mockResolvedValue({ data: { portfolio: [] } });
    render(<PortfolioList />);

    await waitFor(() => {
      expect(screen.getByText(/No portfolio items found/)).toBeInTheDocument();
    });
  });

  test('refresh button calls fetch function', async () => {
    axios.get.mockResolvedValue({ data: mockPortfolioData });
    render(<PortfolioList />);

    await waitFor(() => {
      expect(screen.getByText('AAPL')).toBeInTheDocument();
    });

    const refreshBtn = screen.getByText('Refresh');
    fireEvent.click(refreshBtn);

    expect(axios.get).toHaveBeenCalledTimes(2);
  });
});
