package org.example.stockpotrfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.example.stockpotrfolio.dto.PortfolioSummary;
import org.example.stockpotrfolio.entity.PortfolioItem;
import org.example.stockpotrfolio.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for portfolio management operations.
 * Handles buying stocks, managing portfolio items, and calculating weighted average prices.
 */
@Service
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    /**
     * Buy stock and update or create a portfolio item.
     * If the stock already exists in the portfolio, updates the quantity and recalculates
     * the weighted average buy price. If it doesn't exist, creates a new portfolio item.
     *
     * Formula for weighted average price:
     * NewPrice = (OldQty × OldPrice + NewQty × BuyPrice) / (OldQty + NewQty)
     *
     * @param symbol the stock symbol (e.g., "AAPL")
     * @param buyPrice the price per share being bought
     * @param quantity the number of shares being bought
     * @return the updated or newly created PortfolioItem
     * @throws IllegalArgumentException if any parameter is invalid
     */
    @Transactional
    public PortfolioItem buyStock(String symbol, Double buyPrice, Integer quantity) {
        // Validate input parameters
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Invalid symbol provided: null or empty");
            throw new IllegalArgumentException("Stock symbol cannot be null or empty");
        }

        if (buyPrice == null || buyPrice <= 0) {
            log.warn("Invalid buy price provided: {}", buyPrice);
            throw new IllegalArgumentException("Buy price must be greater than 0");
        }

        if (quantity == null || quantity <= 0) {
            log.warn("Invalid quantity provided: {}", quantity);
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        final String finalSymbol = symbol.toUpperCase().trim();
        log.info("Processing buy stock request - Symbol: {}, Price: ${}, Quantity: {}", finalSymbol, buyPrice, quantity);

        // Check if the stock already exists in the portfolio using efficient repository method
        Optional<PortfolioItem> existingItem = portfolioRepository.findBySymbol(finalSymbol);

        PortfolioItem portfolioItem;

        if (existingItem.isPresent()) {
            // Stock already exists - update it with weighted average price
            portfolioItem = updateExistingStock(existingItem.get(), buyPrice, quantity);
        } else {
            // Stock doesn't exist - create a new portfolio item
            portfolioItem = createNewStock(finalSymbol, buyPrice, quantity);
        }

        log.info("Stock purchase completed - Symbol: {}, New Quantity: {}, New Avg Price: ${}",
                finalSymbol, portfolioItem.getQuantity(), portfolioItem.getAverageBuyPrice());

        return portfolioItem;
    }

    /**
     * Create a new portfolio item for a stock that doesn't exist in the portfolio.
     *
     * @param symbol the stock symbol
     * @param buyPrice the purchase price
     * @param quantity the quantity purchased
     * @return the newly created and saved PortfolioItem
     */
    private PortfolioItem createNewStock(String symbol, Double buyPrice, Integer quantity) {
        log.debug("Creating new portfolio item for symbol: {}", symbol);

        PortfolioItem newItem = new PortfolioItem();
        newItem.setSymbol(symbol);
        newItem.setAverageBuyPrice(buyPrice);
        newItem.setQuantity(quantity);

        return portfolioRepository.save(newItem);
    }

    /**
     * Update an existing portfolio item with new stock purchase using weighted average price.
     *
     * Weighted Average Price = (OldQty × OldPrice + NewQty × BuyPrice) / (OldQty + NewQty)
     *
     * @param portfolioItem the existing portfolio item
     * @param buyPrice the price of the new purchase
     * @param newQuantity the quantity being purchased
     * @return the updated and saved PortfolioItem
     */
    private PortfolioItem updateExistingStock(PortfolioItem portfolioItem, Double buyPrice, Integer newQuantity) {
        log.debug("Updating existing portfolio item for symbol: {}", portfolioItem.getSymbol());

        // Get current values
        Integer oldQuantity = portfolioItem.getQuantity();
        Double oldPrice = portfolioItem.getAverageBuyPrice();

        // Calculate weighted average price
        // Formula: NewPrice = (OldQty × OldPrice + NewQty × BuyPrice) / (OldQty + NewQty)
        Double newAveragePrice = ((oldQuantity * oldPrice) + (newQuantity * buyPrice))
                / (double) (oldQuantity + newQuantity);

        // Update portfolio item
        portfolioItem.setQuantity(oldQuantity + newQuantity);
        portfolioItem.setAverageBuyPrice(newAveragePrice);

        log.debug("Weighted average price calculated - Old: ${} (Qty: {}), Buy: ${} (Qty: {}), New: ${} (Qty: {})",
                oldPrice, oldQuantity, buyPrice, newQuantity, newAveragePrice, portfolioItem.getQuantity());

        return portfolioRepository.save(portfolioItem);
    }

    /**
     * Retrieve the entire portfolio.
     * Returns all portfolio items currently held.
     *
     * @return a list of all PortfolioItem objects
     */
    @Transactional(readOnly = true)
    public List<PortfolioItem> getPortfolio() {
        log.info("Retrieving entire portfolio");
        List<PortfolioItem> portfolio = portfolioRepository.findAll();
        log.info("Portfolio contains {} items", portfolio.size());
        return portfolio;
    }

    /**
     * Get a specific portfolio item by symbol.
     *
     * @param symbol the stock symbol
     * @return Optional containing the PortfolioItem if found, otherwise empty
     */
    @Transactional(readOnly = true)
    public Optional<PortfolioItem> getPortfolioItemBySymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Invalid symbol provided: null or empty");
            return Optional.empty();
        }

        final String finalSymbol = symbol.toUpperCase().trim();
        log.info("Retrieving portfolio item for symbol: {}", finalSymbol);

        return portfolioRepository.findBySymbol(finalSymbol);
    }

    /**
     * Get the total value of the portfolio at current prices.
     *
     * @param currentPrices a map of symbol to current price
     * @return the total portfolio value
     */
    @Transactional(readOnly = true)
    public Double getPortfolioValue(java.util.Map<String, Double> currentPrices) {
        log.info("Calculating portfolio value with current prices");

        Double totalValue = portfolioRepository.findAll().stream()
                .mapToDouble(item -> {
                    Double currentPrice = currentPrices.getOrDefault(item.getSymbol(), 0.0);
                    return item.getQuantity() * currentPrice;
                })
                .sum();

        log.info("Total portfolio value: ${}", totalValue);
        return totalValue;
    }

    /**
     * Get a summary of the entire portfolio with live price information.
     * Combines portfolio items from the database with live stock prices.
     *
     * @param currentPrices a map of symbol to current live price
     * @return a list of PortfolioSummary objects containing combined data
     */
    @Transactional(readOnly = true)
    public List<PortfolioSummary> getPortfolioSummary(Map<String, Double> currentPrices) {
        log.info("Generating portfolio summary with live prices");

        List<PortfolioSummary> summary = portfolioRepository.findAll().stream()
                .map(item -> {
                    Double currentPrice = currentPrices.getOrDefault(item.getSymbol(), 0.0);
                    return PortfolioSummary.from(
                            item.getSymbol(),
                            item.getQuantity(),
                            item.getAverageBuyPrice(),
                            currentPrice
                    );
                })
                .toList();

        log.info("Portfolio summary generated with {} items", summary.size());
        return summary;
    }

    /**
     * Get a summary for a specific portfolio item with live price information.
     *
     * @param symbol the stock symbol
     * @param currentPrice the current live price
     * @return Optional containing PortfolioSummary if the item exists, otherwise empty
     */
    @Transactional(readOnly = true)
    public Optional<PortfolioSummary> getPortfolioSummaryBySymbol(String symbol, Double currentPrice) {
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Invalid symbol provided: null or empty");
            return Optional.empty();
        }

        if (currentPrice == null || currentPrice < 0) {
            log.warn("Invalid current price provided: {}", currentPrice);
            return Optional.empty();
        }

        final String finalSymbol = symbol.toUpperCase().trim();
        log.info("Generating portfolio summary for symbol: {} with current price: ${}", finalSymbol, currentPrice);

        Optional<PortfolioItem> item = getPortfolioItemBySymbol(finalSymbol);

        return item.map(portfolioItem -> PortfolioSummary.from(
                portfolioItem.getSymbol(),
                portfolioItem.getQuantity(),
                portfolioItem.getAverageBuyPrice(),
                currentPrice
        ));
    }

    /**
     * Calculate the total portfolio gain/loss in dollars.
     *
     * @param currentPrices a map of symbol to current live price
     * @return the total absolute profit/loss in dollars (positive for gain, negative for loss)
     */
    @Transactional(readOnly = true)
    public Double getTotalPortfolioGainLoss(Map<String, Double> currentPrices) {
        log.info("Calculating total portfolio gain/loss");

        Double totalGainLoss = getPortfolioSummary(currentPrices).stream()
                .mapToDouble(PortfolioSummary::getAbsoluteProfitOrLoss)
                .sum();

        log.info("Total portfolio gain/loss: ${}", totalGainLoss);
        return totalGainLoss;
    }

    /**
     * Calculate the average profit/loss percentage across the entire portfolio.
     * Weighted by the current value of each holding.
     *
     * @param currentPrices a map of symbol to current live price
     * @return the weighted average profit/loss percentage
     */
    @Transactional(readOnly = true)
    public Double getWeightedAverageGainLossPercentage(Map<String, Double> currentPrices) {
        log.info("Calculating weighted average portfolio gain/loss percentage");

        List<PortfolioSummary> summaries = getPortfolioSummary(currentPrices);

        if (summaries.isEmpty()) {
            log.warn("Portfolio is empty");
            return 0.0;
        }

        Double totalValue = summaries.stream()
                .mapToDouble(PortfolioSummary::getCurrentTotalValue)
                .sum();

        if (totalValue == 0) {
            return 0.0;
        }

        Double weightedGainLoss = summaries.stream()
                .mapToDouble(summary -> (summary.profitOrLossPercentage() * summary.getCurrentTotalValue()) / totalValue)
                .sum();

        log.info("Weighted average portfolio gain/loss percentage: {}", weightedGainLoss);
        return weightedGainLoss;
    }

    /**
     * Sell stock from the portfolio.
     * Reduces the quantity of shares owned. If all shares are sold, the portfolio item is removed.
     *
     * @param symbol the stock symbol
     * @param quantity the number of shares to sell
     * @return the updated PortfolioItem, or null if all shares were sold (item removed)
     * @throws IllegalArgumentException if parameters are invalid or insufficient shares
     */
    @Transactional
    public PortfolioItem sellStock(String symbol, Integer quantity) {
        // Validate input parameters
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Invalid symbol provided: null or empty");
            throw new IllegalArgumentException("Stock symbol cannot be null or empty");
        }

        if (quantity == null || quantity <= 0) {
            log.warn("Invalid quantity provided: {}", quantity);
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        final String finalSymbol = symbol.toUpperCase().trim();
        log.info("Processing sell stock request - Symbol: {}, Quantity: {}", finalSymbol, quantity);

        // Check if the stock exists in the portfolio using efficient repository method
        Optional<PortfolioItem> existingItem = portfolioRepository.findBySymbol(finalSymbol);

        if (existingItem.isEmpty()) {
            log.warn("Stock symbol not found in portfolio: {}", finalSymbol);
            throw new IllegalArgumentException("Stock symbol not found in portfolio: " + finalSymbol);
        }

        PortfolioItem portfolioItem = existingItem.get();
        Integer currentQuantity = portfolioItem.getQuantity();

        if (quantity > currentQuantity) {
            log.warn("Insufficient shares - Attempting to sell {} but only {} available", quantity, currentQuantity);
            throw new IllegalArgumentException("Insufficient shares. You have " + currentQuantity + " shares but trying to sell " + quantity);
        }

        Integer newQuantity = currentQuantity - quantity;

        if (newQuantity == 0) {
            // Remove the item entirely if all shares are sold
            log.info("All shares sold for symbol: {}. Removing portfolio item.", finalSymbol);
            portfolioRepository.delete(portfolioItem);
            return null;
        } else {
            // Update the quantity
            portfolioItem.setQuantity(newQuantity);
            portfolioRepository.save(portfolioItem);
            log.info("Stock sale completed - Symbol: {}, Quantity Sold: {}, Remaining: {}",
                    finalSymbol, quantity, newQuantity);
            return portfolioItem;
        }
    }
}
