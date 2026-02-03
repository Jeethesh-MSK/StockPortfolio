package org.example.stockpotrfolio.repository;

import org.example.stockpotrfolio.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository interface for PortfolioItem entity.
 * Provides CRUD operations and custom query methods for portfolio items.
 */
@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioItem, Long> {

    /**
     * Find a portfolio item by its stock symbol.
     *
     * @param symbol the stock symbol (e.g., "AAPL")
     * @return Optional containing the PortfolioItem if found
     */
    Optional<PortfolioItem> findBySymbol(String symbol);

    /**
     * Check if a portfolio item exists for the given symbol.
     *
     * @param symbol the stock symbol
     * @return true if exists, false otherwise
     */
    boolean existsBySymbol(String symbol);
}
