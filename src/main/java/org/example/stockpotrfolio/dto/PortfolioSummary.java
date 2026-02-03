package org.example.stockpotrfolio.dto;

/**
 * Data Transfer Object (Record) for portfolio summary information.
 * Combines portfolio item data from the database with live stock prices
 * to provide a unified view of holdings and their current values.
 *
 * This record is immutable and ideal for transferring data between layers.
 */
public record PortfolioSummary(
        String symbol,
        Integer quantity,
        Double averageBuyPrice,
        Double currentLivePrice,
        Double profitOrLossPercentage
) {

    /**
     * Compact constructor for validation.
     * Ensures all fields meet business requirements.
     */
    public PortfolioSummary {
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (averageBuyPrice == null || averageBuyPrice <= 0) {
            throw new IllegalArgumentException("Average buy price must be greater than 0");
        }
        if (currentLivePrice == null || currentLivePrice < 0) {
            throw new IllegalArgumentException("Current live price cannot be negative");
        }
        if (profitOrLossPercentage == null) {
            throw new IllegalArgumentException("Profit or loss percentage cannot be null");
        }
    }

    /**
     * Factory method to create a PortfolioSummary from a PortfolioItem and current price.
     * Calculates profit/loss percentage automatically.
     *
     * Formula for profit/loss percentage:
     * (CurrentPrice - AverageBuyPrice) / AverageBuyPrice * 100
     *
     * @param symbol the stock symbol
     * @param quantity the number of shares owned
     * @param averageBuyPrice the average purchase price per share
     * @param currentLivePrice the current market price per share
     * @return a new PortfolioSummary instance with calculated profit/loss percentage
     */
    public static PortfolioSummary from(String symbol, Integer quantity, Double averageBuyPrice, Double currentLivePrice) {
        // Calculate profit/loss percentage
        Double profitOrLossPercentage = ((currentLivePrice - averageBuyPrice) / averageBuyPrice) * 100;

        return new PortfolioSummary(symbol, quantity, averageBuyPrice, currentLivePrice, profitOrLossPercentage);
    }

    /**
     * Get the total invested amount for this holding.
     * Calculated as: quantity × averageBuyPrice
     *
     * @return the total amount invested in this stock
     */
    public Double getTotalInvested() {
        return quantity * averageBuyPrice;
    }

    /**
     * Get the current total value of this holding at market price.
     * Calculated as: quantity × currentLivePrice
     *
     * @return the current total market value of this stock
     */
    public Double getCurrentTotalValue() {
        return quantity * currentLivePrice;
    }

    /**
     * Get the absolute profit or loss amount for this holding.
     * Calculated as: currentTotalValue - totalInvested
     *
     * @return the absolute profit/loss amount (positive for gain, negative for loss)
     */
    public Double getAbsoluteProfitOrLoss() {
        return getCurrentTotalValue() - getTotalInvested();
    }

    /**
     * Check if this holding is currently profitable.
     *
     * @return true if profit/loss percentage is positive, false otherwise
     */
    public boolean isProfit() {
        return profitOrLossPercentage >= 0;
    }

    /**
     * Get a formatted string representation of profit/loss percentage.
     * Includes the % symbol and shows 2 decimal places.
     *
     * @return formatted profit/loss percentage string
     */
    public String getFormattedProfitOrLossPercentage() {
        return String.format("%.2f%%", profitOrLossPercentage);
    }

    /**
     * Get a formatted string representation of the current total value.
     * Shows the value with a $ symbol and 2 decimal places.
     *
     * @return formatted current total value string
     */
    public String getFormattedCurrentTotalValue() {
        return String.format("$%.2f", getCurrentTotalValue());
    }

    /**
     * Get a formatted string representation of the absolute profit/loss.
     * Shows the value with a $ symbol and 2 decimal places.
     *
     * @return formatted absolute profit/loss string
     */
    public String getFormattedAbsoluteProfitOrLoss() {
        return String.format("%s$%.2f", isProfit() ? "+" : "", getAbsoluteProfitOrLoss());
    }
}
