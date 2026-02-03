import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import PortfolioItem from './PortfolioItem';
import * as portfolioAPI from '../api/portfolioAPI';

jest.mock('../api/portfolioAPI');

describe('PortfolioItem - Buy/Sell Feature', () => {
  const mockItem = {
    symbol: 'AAPL',
    quantity: 10,
    averageBuyPrice: 150.00,
    currentLivePrice: 160.00,
    profitOrLossPercentage: 6.67
  };

  const mockOnTransactionComplete = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Buy Button', () => {
    it('should render buy button', () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );
      expect(screen.getByText('💰 Buy')).toBeInTheDocument();
    });

    it('should open modal when buy button is clicked', () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      const buyButton = screen.getByText('💰 Buy');
      fireEvent.click(buyButton);

      expect(screen.getByText('Buy AAPL')).toBeInTheDocument();
    });

    it('should successfully buy stock', async () => {
      portfolioAPI.buyStock.mockResolvedValue({
        status: 'success',
        message: 'Stock purchase completed successfully',
        symbol: 'AAPL',
        quantity: 15,
        averagePrice: 152.50
      });

      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      // Open modal
      fireEvent.click(screen.getByText('💰 Buy'));

      // Enter quantity
      const quantityInput = screen.getByPlaceholderText('Enter quantity');
      await userEvent.type(quantityInput, '5');

      // Enter price
      const priceInput = screen.getByPlaceholderText('Enter price');
      await userEvent.type(priceInput, '155.00');

      // Click Buy
      fireEvent.click(screen.getByRole('button', { name: /Buy/i }));

      await waitFor(() => {
        expect(portfolioAPI.buyStock).toHaveBeenCalledWith('AAPL', 5, 155.00);
      });
    });

    it('should show error when quantity is invalid', () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      fireEvent.click(screen.getByText('💰 Buy'));

      const buyConfirmButton = screen.getByRole('button', { name: /Buy/i });
      fireEvent.click(buyConfirmButton);

      expect(screen.getByText('Please enter a valid quantity')).toBeInTheDocument();
    });

    it('should show error when price is invalid', async () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      fireEvent.click(screen.getByText('💰 Buy'));

      const quantityInput = screen.getByPlaceholderText('Enter quantity');
      await userEvent.type(quantityInput, '5');

      const priceInput = screen.getByPlaceholderText('Enter price');
      await userEvent.type(priceInput, '-10');

      const buyConfirmButton = screen.getByRole('button', { name: /Buy/i });
      fireEvent.click(buyConfirmButton);

      expect(screen.getByText('Please enter a valid price')).toBeInTheDocument();
    });
  });

  describe('Sell Button', () => {
    it('should render sell button', () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );
      expect(screen.getByText('📤 Sell')).toBeInTheDocument();
    });

    it('should open modal when sell button is clicked', () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      const sellButton = screen.getByText('📤 Sell');
      fireEvent.click(sellButton);

      expect(screen.getByText('Sell AAPL')).toBeInTheDocument();
    });

    it('should successfully sell stock', async () => {
      portfolioAPI.sellStock.mockResolvedValue({
        status: 'success',
        message: 'Shares sold successfully. Remaining quantity: 5',
        symbol: 'AAPL',
        quantity: 5,
        averagePrice: 150.00
      });

      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      // Open modal
      fireEvent.click(screen.getByText('📤 Sell'));

      // Enter quantity
      const quantityInput = screen.getByPlaceholderText('Enter quantity');
      await userEvent.type(quantityInput, '5');

      // Click Sell
      fireEvent.click(screen.getByRole('button', { name: /Sell/i }));

      await waitFor(() => {
        expect(portfolioAPI.sellStock).toHaveBeenCalledWith('AAPL', 5);
      });
    });

    it('should show error when selling more shares than owned', async () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      fireEvent.click(screen.getByText('📤 Sell'));

      const quantityInput = screen.getByPlaceholderText('Enter quantity');
      await userEvent.type(quantityInput, '20'); // More than 10 owned

      const sellConfirmButton = screen.getByRole('button', { name: /Sell/i });
      fireEvent.click(sellConfirmButton);

      expect(screen.getByText('Cannot sell 20 shares. You only have 10 shares.')).toBeInTheDocument();
    });

    it('should show error when quantity is invalid', () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      fireEvent.click(screen.getByText('📤 Sell'));

      const sellConfirmButton = screen.getByRole('button', { name: /Sell/i });
      fireEvent.click(sellConfirmButton);

      expect(screen.getByText('Please enter a valid quantity')).toBeInTheDocument();
    });
  });

  describe('Modal Interactions', () => {
    it('should close modal when close button is clicked', () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      fireEvent.click(screen.getByText('💰 Buy'));
      expect(screen.getByText('Buy AAPL')).toBeInTheDocument();

      const closeButton = screen.getByRole('button', { name: /×/i });
      fireEvent.click(closeButton);

      expect(screen.queryByText('Buy AAPL')).not.toBeInTheDocument();
    });

    it('should close modal when cancel button is clicked', () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      fireEvent.click(screen.getByText('💰 Buy'));

      const cancelButton = screen.getByRole('button', { name: /Cancel/i });
      fireEvent.click(cancelButton);

      expect(screen.queryByText('Buy AAPL')).not.toBeInTheDocument();
    });

    it('should show transaction summary', async () => {
      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      fireEvent.click(screen.getByText('💰 Buy'));

      const quantityInput = screen.getByPlaceholderText('Enter quantity');
      await userEvent.type(quantityInput, '5');

      const priceInput = screen.getByPlaceholderText('Enter price');
      await userEvent.type(priceInput, '155.00');

      expect(screen.getByText('5 shares')).toBeInTheDocument();
      expect(screen.getByText('$155.00')).toBeInTheDocument();
      expect(screen.getByText('$775.00')).toBeInTheDocument(); // 5 * 155
    });

    it('should call onTransactionComplete after successful transaction', async () => {
      portfolioAPI.buyStock.mockResolvedValue({
        status: 'success',
        message: 'Stock purchase completed successfully',
        symbol: 'AAPL',
        quantity: 15,
        averagePrice: 152.50
      });

      render(
        <PortfolioItem
          {...mockItem}
          onTransactionComplete={mockOnTransactionComplete}
        />
      );

      fireEvent.click(screen.getByText('💰 Buy'));

      const quantityInput = screen.getByPlaceholderText('Enter quantity');
      await userEvent.type(quantityInput, '5');

      const priceInput = screen.getByPlaceholderText('Enter price');
      await userEvent.type(priceInput, '155.00');

      fireEvent.click(screen.getByRole('button', { name: /^Buy$/i }));

      await waitFor(() => {
        expect(mockOnTransactionComplete).toHaveBeenCalled();
      });
    });
  });
});
