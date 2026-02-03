import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import PortfolioItem from '../components/PortfolioItem';

describe('PortfolioItem Component', () => {
  const mockProps = {
    symbol: 'AAPL',
    quantity: 10,
    averageBuyPrice: 150.0,
    currentLivePrice: 165.0,
    profitOrLossPercentage: 10.0
  };

  test('renders portfolio item with correct symbol', () => {
    render(<PortfolioItem {...mockProps} />);
    expect(screen.getByText('AAPL')).toBeInTheDocument();
  });

  test('displays correct quantity', () => {
    render(<PortfolioItem {...mockProps} />);
    expect(screen.getByText(/10 shares/)).toBeInTheDocument();
  });

  test('displays correct average buy price', () => {
    render(<PortfolioItem {...mockProps} />);
    expect(screen.getByText('$150.00')).toBeInTheDocument();
  });

  test('displays correct current price', () => {
    render(<PortfolioItem {...mockProps} />);
    expect(screen.getByText('$165.00')).toBeInTheDocument();
  });

  test('calculates and displays total cost correctly', () => {
    render(<PortfolioItem {...mockProps} />);
    const totalCost = 10 * 150.0;
    expect(screen.getByText(`$${totalCost.toFixed(2)}`)).toBeInTheDocument();
  });

  test('calculates and displays current value correctly', () => {
    render(<PortfolioItem {...mockProps} />);
    const totalValue = 10 * 165.0;
    expect(screen.getByText(`$${totalValue.toFixed(2)}`)).toBeInTheDocument();
  });

  test('calculates and displays profit/loss correctly', () => {
    render(<PortfolioItem {...mockProps} />);
    const profitLoss = (10 * 165.0) - (10 * 150.0);
    expect(screen.getByText(`$${profitLoss.toFixed(2)}`)).toBeInTheDocument();
  });

  test('displays profit badge for positive gain', () => {
    render(<PortfolioItem {...mockProps} />);
    expect(screen.getByText(/10.00%/)).toBeInTheDocument();
  });

  test('shows loss badge for negative gain', () => {
    const lossProps = {
      ...mockProps,
      currentLivePrice: 140.0,
      profitOrLossPercentage: -6.67
    };
    render(<PortfolioItem {...lossProps} />);
    expect(screen.getByText(/-6.67%/)).toBeInTheDocument();
  });
});
