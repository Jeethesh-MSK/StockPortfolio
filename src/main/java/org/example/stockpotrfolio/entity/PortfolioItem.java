package org.example.stockpotrfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * JPA Entity representing a stock item in a portfolio.
 * Contains information about a stock holding including symbol, purchase price, and quantity.
 */
@Entity
@Table(name = "portfolio_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItem {

    /**
     * Unique identifier for the portfolio item.
     * Auto-generated using identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stock symbol (e.g., "AAPL", "GOOGL").
     * Cannot be null and must be unique in the portfolio.
     */
    @NotBlank(message = "Stock symbol cannot be blank")
    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    /**
     * Average purchase price of the stock.
     * Represents the average price paid per share.
     */
    @NotNull(message = "Average buy price is required")
    @Positive(message = "Average buy price must be positive")
    @Column(nullable = false)
    private Double averageBuyPrice;

    /**
     * Quantity of shares owned.
     * Represents the number of shares in the portfolio.
     */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;
}
