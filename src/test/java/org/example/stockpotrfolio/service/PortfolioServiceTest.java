package org.example.stockpotrfolio.service;

import org.example.stockpotrfolio.dto.PortfolioSummary;
import org.example.stockpotrfolio.entity.PortfolioItem;
import org.example.stockpotrfolio.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortfolioService.
 *
 * Tests cover all public methods with various scenarios:
 * - Happy path / successful operations
 * - Invalid input handling
 * - Edge cases (empty lists, null values)
 * - Exception scenarios
 *
 * Uses Mockito to mock the PortfolioRepository dependency,
 * allowing us to test business logic in isolation without database access.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService Unit Tests")
class PortfolioServiceTest {

    /**
     * Mock of PortfolioRepository - we mock this to isolate the service layer
     * and avoid actual database operations during unit tests.
     */
    @Mock
    private PortfolioRepository portfolioRepository;

    /**
     * The service under test. Mockito will inject the mocked repository.
     */
    @InjectMocks
    private PortfolioService portfolioService;

    /**
     * Captor to capture PortfolioItem arguments passed to repository methods.
     * Useful for verifying the exact state of objects being saved.
     */
    @Captor
    private ArgumentCaptor<PortfolioItem> portfolioItemCaptor;

    // Test fixtures
    private PortfolioItem existingItem;
    private static final String SYMBOL_AAPL = "AAPL";
    private static final String SYMBOL_GOOGL = "GOOGL";
    private static final Double BUY_PRICE = 150.0;
    private static final Integer QUANTITY = 10;

    @BeforeEach
    void setUp() {
        // Create a standard existing portfolio item for tests that need it
        existingItem = new PortfolioItem();
        existingItem.setId(1L);
        existingItem.setSymbol(SYMBOL_AAPL);
        existingItem.setAverageBuyPrice(100.0);
        existingItem.setQuantity(10);
    }

    /**
     * Tests for buyStock() method.
     * This method handles both creating new portfolio items and updating existing ones.
     */
    @Nested
    @DisplayName("buyStock() Tests")
    class BuyStockTests {

        @Test
        @DisplayName("Should create new portfolio item when stock doesn't exist")
        void buyStock_WhenStockDoesNotExist_ShouldCreateNewItem() {
            // Arrange: Stock doesn't exist in portfolio
            when(portfolioRepository.findBySymbol(SYMBOL_AAPL)).thenReturn(Optional.empty());
            when(portfolioRepository.save(any(PortfolioItem.class))).thenAnswer(invocation -> {
                PortfolioItem item = invocation.getArgument(0);
                item.setId(1L);
                return item;
            });

            // Act
            PortfolioItem result = portfolioService.buyStock(SYMBOL_AAPL, BUY_PRICE, QUANTITY);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(SYMBOL_AAPL);
            assertThat(result.getAverageBuyPrice()).isEqualTo(BUY_PRICE);
            assertThat(result.getQuantity()).isEqualTo(QUANTITY);

            // Verify repository interactions
            verify(portfolioRepository).findBySymbol(SYMBOL_AAPL);
            verify(portfolioRepository).save(portfolioItemCaptor.capture());

            PortfolioItem savedItem = portfolioItemCaptor.getValue();
            assertThat(savedItem.getSymbol()).isEqualTo(SYMBOL_AAPL);
            assertThat(savedItem.getAverageBuyPrice()).isEqualTo(BUY_PRICE);
            assertThat(savedItem.getQuantity()).isEqualTo(QUANTITY);
        }

        @Test
        @DisplayName("Should update existing item with weighted average price when stock exists")
        void buyStock_WhenStockExists_ShouldUpdateWithWeightedAverage() {
            // Arrange: Stock already exists with 10 shares at $100
            when(portfolioRepository.findBySymbol(SYMBOL_AAPL)).thenReturn(Optional.of(existingItem));
            when(portfolioRepository.save(any(PortfolioItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: Buy 10 more shares at $200
            PortfolioItem result = portfolioService.buyStock(SYMBOL_AAPL, 200.0, 10);

            // Assert: Weighted average = (10 * 100 + 10 * 200) / 20 = 150
            assertThat(result).isNotNull();
            assertThat(result.getQuantity()).isEqualTo(20);
            assertThat(result.getAverageBuyPrice()).isCloseTo(150.0, within(0.001));

            verify(portfolioRepository).save(any(PortfolioItem.class));
        }

        @Test
        @DisplayName("Should convert symbol to uppercase and trim whitespace")
        void buyStock_WithLowercaseSymbol_ShouldNormalizeToUppercase() {
            // Arrange
            when(portfolioRepository.findBySymbol("AAPL")).thenReturn(Optional.empty());
            when(portfolioRepository.save(any(PortfolioItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: Pass lowercase symbol with whitespace
            PortfolioItem result = portfolioService.buyStock("  aapl  ", BUY_PRICE, QUANTITY);

            // Assert: Symbol should be normalized
            assertThat(result.getSymbol()).isEqualTo("AAPL");
            verify(portfolioRepository).findBySymbol("AAPL");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is null")
        void buyStock_WithNullSymbol_ShouldThrowException() {
            // Act & Assert
            assertThatThrownBy(() -> portfolioService.buyStock(null, BUY_PRICE, QUANTITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol cannot be null or empty");

            // Verify no repository interactions occurred
            verify(portfolioRepository, never()).findBySymbol(anyString());
            verify(portfolioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is empty")
        void buyStock_WithEmptySymbol_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.buyStock("", BUY_PRICE, QUANTITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol cannot be null or empty");

            verify(portfolioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol contains only whitespace")
        void buyStock_WithWhitespaceOnlySymbol_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.buyStock("   ", BUY_PRICE, QUANTITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when buyPrice is null")
        void buyStock_WithNullBuyPrice_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.buyStock(SYMBOL_AAPL, null, QUANTITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Buy price must be greater than 0");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when buyPrice is zero")
        void buyStock_WithZeroBuyPrice_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.buyStock(SYMBOL_AAPL, 0.0, QUANTITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Buy price must be greater than 0");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when buyPrice is negative")
        void buyStock_WithNegativeBuyPrice_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.buyStock(SYMBOL_AAPL, -100.0, QUANTITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Buy price must be greater than 0");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is null")
        void buyStock_WithNullQuantity_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.buyStock(SYMBOL_AAPL, BUY_PRICE, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is zero")
        void buyStock_WithZeroQuantity_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.buyStock(SYMBOL_AAPL, BUY_PRICE, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is negative")
        void buyStock_WithNegativeQuantity_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.buyStock(SYMBOL_AAPL, BUY_PRICE, -5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("Should correctly calculate weighted average for unequal quantities")
        void buyStock_WithUnequalQuantities_ShouldCalculateCorrectWeightedAverage() {
            // Arrange: Existing 5 shares at $100
            existingItem.setQuantity(5);
            existingItem.setAverageBuyPrice(100.0);
            when(portfolioRepository.findBySymbol(SYMBOL_AAPL)).thenReturn(Optional.of(existingItem));
            when(portfolioRepository.save(any(PortfolioItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: Buy 15 more shares at $200
            PortfolioItem result = portfolioService.buyStock(SYMBOL_AAPL, 200.0, 15);

            // Assert: Weighted average = (5 * 100 + 15 * 200) / 20 = (500 + 3000) / 20 = 175
            assertThat(result.getQuantity()).isEqualTo(20);
            assertThat(result.getAverageBuyPrice()).isCloseTo(175.0, within(0.001));
        }
    }

    /**
     * Tests for sellStock() method.
     * Handles selling shares from existing portfolio items.
     */
    @Nested
    @DisplayName("sellStock() Tests")
    class SellStockTests {

        @Test
        @DisplayName("Should reduce quantity when selling partial shares")
        void sellStock_WhenSellingPartialShares_ShouldUpdateQuantity() {
            // Arrange
            when(portfolioRepository.findBySymbol(SYMBOL_AAPL)).thenReturn(Optional.of(existingItem));
            when(portfolioRepository.save(any(PortfolioItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: Sell 5 out of 10 shares
            PortfolioItem result = portfolioService.sellStock(SYMBOL_AAPL, 5);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getQuantity()).isEqualTo(5);
            // Average buy price should remain unchanged
            assertThat(result.getAverageBuyPrice()).isEqualTo(100.0);

            verify(portfolioRepository).save(any(PortfolioItem.class));
            verify(portfolioRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should delete portfolio item when selling all shares")
        void sellStock_WhenSellingAllShares_ShouldDeleteItem() {
            // Arrange
            when(portfolioRepository.findBySymbol(SYMBOL_AAPL)).thenReturn(Optional.of(existingItem));
            doNothing().when(portfolioRepository).delete(any(PortfolioItem.class));

            // Act: Sell all 10 shares
            PortfolioItem result = portfolioService.sellStock(SYMBOL_AAPL, 10);

            // Assert: Returns null when item is deleted
            assertThat(result).isNull();

            verify(portfolioRepository).delete(existingItem);
            verify(portfolioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is null")
        void sellStock_WithNullSymbol_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.sellStock(null, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is empty")
        void sellStock_WithEmptySymbol_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.sellStock("", 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is null")
        void sellStock_WithNullQuantity_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.sellStock(SYMBOL_AAPL, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is zero")
        void sellStock_WithZeroQuantity_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.sellStock(SYMBOL_AAPL, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is negative")
        void sellStock_WithNegativeQuantity_ShouldThrowException() {
            assertThatThrownBy(() -> portfolioService.sellStock(SYMBOL_AAPL, -5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when stock not found in portfolio")
        void sellStock_WhenStockNotFound_ShouldThrowException() {
            // Arrange
            when(portfolioRepository.findBySymbol(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> portfolioService.sellStock("UNKNOWN", 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol not found in portfolio");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when selling more shares than owned")
        void sellStock_WhenInsufficientShares_ShouldThrowException() {
            // Arrange
            when(portfolioRepository.findBySymbol(SYMBOL_AAPL)).thenReturn(Optional.of(existingItem));

            // Act & Assert: Try to sell 15 shares when only 10 owned
            assertThatThrownBy(() -> portfolioService.sellStock(SYMBOL_AAPL, 15))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient shares");
        }

        @Test
        @DisplayName("Should normalize symbol to uppercase and trim whitespace")
        void sellStock_WithLowercaseSymbol_ShouldNormalizeToUppercase() {
            // Arrange
            when(portfolioRepository.findBySymbol("AAPL")).thenReturn(Optional.of(existingItem));
            when(portfolioRepository.save(any(PortfolioItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PortfolioItem result = portfolioService.sellStock("  aapl  ", 5);

            // Assert
            assertThat(result).isNotNull();
            verify(portfolioRepository).findBySymbol("AAPL");
        }
    }

    /**
     * Tests for getPortfolio() method.
     * Retrieves all portfolio items.
     */
    @Nested
    @DisplayName("getPortfolio() Tests")
    class GetPortfolioTests {

        @Test
        @DisplayName("Should return all portfolio items")
        void getPortfolio_ShouldReturnAllItems() {
            // Arrange
            PortfolioItem item2 = new PortfolioItem(2L, SYMBOL_GOOGL, 2500.0, 5);
            List<PortfolioItem> items = List.of(existingItem, item2);
            when(portfolioRepository.findAll()).thenReturn(items);

            // Act
            List<PortfolioItem> result = portfolioService.getPortfolio();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(items);
            verify(portfolioRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when portfolio is empty")
        void getPortfolio_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(portfolioRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<PortfolioItem> result = portfolioService.getPortfolio();

            // Assert
            assertThat(result).isEmpty();
            verify(portfolioRepository).findAll();
        }
    }

    /**
     * Tests for getPortfolioItemBySymbol() method.
     * Retrieves a specific portfolio item by its stock symbol.
     */
    @Nested
    @DisplayName("getPortfolioItemBySymbol() Tests")
    class GetPortfolioItemBySymbolTests {

        @Test
        @DisplayName("Should return portfolio item when found")
        void getPortfolioItemBySymbol_WhenFound_ShouldReturnItem() {
            // Arrange
            when(portfolioRepository.findBySymbol(SYMBOL_AAPL)).thenReturn(Optional.of(existingItem));

            // Act
            Optional<PortfolioItem> result = portfolioService.getPortfolioItemBySymbol(SYMBOL_AAPL);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(existingItem);
            verify(portfolioRepository).findBySymbol(SYMBOL_AAPL);
        }

        @Test
        @DisplayName("Should return empty Optional when not found")
        void getPortfolioItemBySymbol_WhenNotFound_ShouldReturnEmpty() {
            // Arrange
            when(portfolioRepository.findBySymbol(anyString())).thenReturn(Optional.empty());

            // Act
            Optional<PortfolioItem> result = portfolioService.getPortfolioItemBySymbol("UNKNOWN");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when symbol is null")
        void getPortfolioItemBySymbol_WithNullSymbol_ShouldReturnEmpty() {
            // Act
            Optional<PortfolioItem> result = portfolioService.getPortfolioItemBySymbol(null);

            // Assert
            assertThat(result).isEmpty();
            verify(portfolioRepository, never()).findBySymbol(anyString());
        }

        @Test
        @DisplayName("Should return empty Optional when symbol is empty")
        void getPortfolioItemBySymbol_WithEmptySymbol_ShouldReturnEmpty() {
            Optional<PortfolioItem> result = portfolioService.getPortfolioItemBySymbol("");
            assertThat(result).isEmpty();
            verify(portfolioRepository, never()).findBySymbol(anyString());
        }

        @Test
        @DisplayName("Should normalize symbol to uppercase")
        void getPortfolioItemBySymbol_WithLowercaseSymbol_ShouldNormalize() {
            // Arrange
            when(portfolioRepository.findBySymbol("AAPL")).thenReturn(Optional.of(existingItem));

            // Act
            Optional<PortfolioItem> result = portfolioService.getPortfolioItemBySymbol("  aapl  ");

            // Assert
            assertThat(result).isPresent();
            verify(portfolioRepository).findBySymbol("AAPL");
        }
    }

    /**
     * Tests for getPortfolioValue() method.
     * Calculates total portfolio value using provided current prices.
     */
    @Nested
    @DisplayName("getPortfolioValue() Tests")
    class GetPortfolioValueTests {

        @Test
        @DisplayName("Should calculate total portfolio value correctly")
        void getPortfolioValue_ShouldCalculateCorrectly() {
            // Arrange
            PortfolioItem item2 = new PortfolioItem(2L, SYMBOL_GOOGL, 2500.0, 5);
            when(portfolioRepository.findAll()).thenReturn(List.of(existingItem, item2));

            Map<String, Double> currentPrices = new HashMap<>();
            currentPrices.put(SYMBOL_AAPL, 150.0);  // 10 shares * $150 = $1500
            currentPrices.put(SYMBOL_GOOGL, 3000.0); // 5 shares * $3000 = $15000

            // Act
            Double totalValue = portfolioService.getPortfolioValue(currentPrices);

            // Assert: $1500 + $15000 = $16500
            assertThat(totalValue).isCloseTo(16500.0, within(0.001));
        }

        @Test
        @DisplayName("Should return zero when portfolio is empty")
        void getPortfolioValue_WhenEmpty_ShouldReturnZero() {
            // Arrange
            when(portfolioRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            Double totalValue = portfolioService.getPortfolioValue(new HashMap<>());

            // Assert
            assertThat(totalValue).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should use zero price when symbol not in currentPrices map")
        void getPortfolioValue_WhenPriceMissing_ShouldUseZero() {
            // Arrange
            when(portfolioRepository.findAll()).thenReturn(List.of(existingItem));

            Map<String, Double> currentPrices = new HashMap<>();
            // AAPL not in prices map

            // Act
            Double totalValue = portfolioService.getPortfolioValue(currentPrices);

            // Assert: Uses 0.0 as default price
            assertThat(totalValue).isEqualTo(0.0);
        }
    }

    /**
     * Tests for getPortfolioSummary() method.
     * Generates portfolio summaries with current price information.
     */
    @Nested
    @DisplayName("getPortfolioSummary() Tests")
    class GetPortfolioSummaryTests {

        @Test
        @DisplayName("Should generate summary with correct profit/loss calculations")
        void getPortfolioSummary_ShouldCalculateProfitLossCorrectly() {
            // Arrange
            when(portfolioRepository.findAll()).thenReturn(List.of(existingItem));

            Map<String, Double> currentPrices = new HashMap<>();
            currentPrices.put(SYMBOL_AAPL, 150.0); // Bought at $100, now $150 = 50% gain

            // Act
            List<PortfolioSummary> summaries = portfolioService.getPortfolioSummary(currentPrices);

            // Assert
            assertThat(summaries).hasSize(1);
            PortfolioSummary summary = summaries.get(0);
            assertThat(summary.symbol()).isEqualTo(SYMBOL_AAPL);
            assertThat(summary.quantity()).isEqualTo(10);
            assertThat(summary.averageBuyPrice()).isEqualTo(100.0);
            assertThat(summary.currentLivePrice()).isEqualTo(150.0);
            assertThat(summary.profitOrLossPercentage()).isCloseTo(50.0, within(0.001));
        }

        @Test
        @DisplayName("Should return empty list when portfolio is empty")
        void getPortfolioSummary_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(portfolioRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<PortfolioSummary> summaries = portfolioService.getPortfolioSummary(new HashMap<>());

            // Assert
            assertThat(summaries).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple portfolio items")
        void getPortfolioSummary_WithMultipleItems_ShouldReturnAllSummaries() {
            // Arrange
            PortfolioItem item2 = new PortfolioItem(2L, SYMBOL_GOOGL, 2500.0, 5);
            when(portfolioRepository.findAll()).thenReturn(List.of(existingItem, item2));

            Map<String, Double> currentPrices = new HashMap<>();
            currentPrices.put(SYMBOL_AAPL, 150.0);
            currentPrices.put(SYMBOL_GOOGL, 2000.0); // Loss scenario

            // Act
            List<PortfolioSummary> summaries = portfolioService.getPortfolioSummary(currentPrices);

            // Assert
            assertThat(summaries).hasSize(2);
        }
    }

    /**
     * Tests for getPortfolioSummaryBySymbol() method.
     * Generates a summary for a specific stock with provided current price.
     */
    @Nested
    @DisplayName("getPortfolioSummaryBySymbol() Tests")
    class GetPortfolioSummaryBySymbolTests {

        @Test
        @DisplayName("Should return summary when item exists")
        void getPortfolioSummaryBySymbol_WhenExists_ShouldReturnSummary() {
            // Arrange
            when(portfolioRepository.findBySymbol(SYMBOL_AAPL)).thenReturn(Optional.of(existingItem));

            // Act
            Optional<PortfolioSummary> result = portfolioService.getPortfolioSummaryBySymbol(SYMBOL_AAPL, 150.0);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().symbol()).isEqualTo(SYMBOL_AAPL);
            assertThat(result.get().currentLivePrice()).isEqualTo(150.0);
        }

        @Test
        @DisplayName("Should return empty when symbol is null")
        void getPortfolioSummaryBySymbol_WithNullSymbol_ShouldReturnEmpty() {
            Optional<PortfolioSummary> result = portfolioService.getPortfolioSummaryBySymbol(null, 150.0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when symbol is empty")
        void getPortfolioSummaryBySymbol_WithEmptySymbol_ShouldReturnEmpty() {
            Optional<PortfolioSummary> result = portfolioService.getPortfolioSummaryBySymbol("", 150.0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when currentPrice is null")
        void getPortfolioSummaryBySymbol_WithNullPrice_ShouldReturnEmpty() {
            Optional<PortfolioSummary> result = portfolioService.getPortfolioSummaryBySymbol(SYMBOL_AAPL, null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when currentPrice is negative")
        void getPortfolioSummaryBySymbol_WithNegativePrice_ShouldReturnEmpty() {
            Optional<PortfolioSummary> result = portfolioService.getPortfolioSummaryBySymbol(SYMBOL_AAPL, -100.0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when item not found")
        void getPortfolioSummaryBySymbol_WhenNotFound_ShouldReturnEmpty() {
            // Arrange
            when(portfolioRepository.findBySymbol(anyString())).thenReturn(Optional.empty());

            // Act
            Optional<PortfolioSummary> result = portfolioService.getPortfolioSummaryBySymbol("UNKNOWN", 150.0);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    /**
     * Tests for getTotalPortfolioGainLoss() method.
     * Calculates total absolute gain/loss across the portfolio.
     */
    @Nested
    @DisplayName("getTotalPortfolioGainLoss() Tests")
    class GetTotalPortfolioGainLossTests {

        @Test
        @DisplayName("Should calculate total gain correctly")
        void getTotalPortfolioGainLoss_WithGains_ShouldReturnPositiveValue() {
            // Arrange: AAPL bought at $100, now $150. 10 shares = $500 gain
            when(portfolioRepository.findAll()).thenReturn(List.of(existingItem));

            Map<String, Double> currentPrices = new HashMap<>();
            currentPrices.put(SYMBOL_AAPL, 150.0);

            // Act
            Double gainLoss = portfolioService.getTotalPortfolioGainLoss(currentPrices);

            // Assert: (150 - 100) * 10 = $500
            assertThat(gainLoss).isCloseTo(500.0, within(0.001));
        }

        @Test
        @DisplayName("Should calculate total loss correctly")
        void getTotalPortfolioGainLoss_WithLosses_ShouldReturnNegativeValue() {
            // Arrange: AAPL bought at $100, now $80. 10 shares = -$200 loss
            when(portfolioRepository.findAll()).thenReturn(List.of(existingItem));

            Map<String, Double> currentPrices = new HashMap<>();
            currentPrices.put(SYMBOL_AAPL, 80.0);

            // Act
            Double gainLoss = portfolioService.getTotalPortfolioGainLoss(currentPrices);

            // Assert: (80 - 100) * 10 = -$200
            assertThat(gainLoss).isCloseTo(-200.0, within(0.001));
        }

        @Test
        @DisplayName("Should return zero when portfolio is empty")
        void getTotalPortfolioGainLoss_WhenEmpty_ShouldReturnZero() {
            when(portfolioRepository.findAll()).thenReturn(Collections.emptyList());

            Double gainLoss = portfolioService.getTotalPortfolioGainLoss(new HashMap<>());

            assertThat(gainLoss).isEqualTo(0.0);
        }
    }

    /**
     * Tests for getWeightedAverageGainLossPercentage() method.
     * Calculates portfolio-wide weighted average gain/loss percentage.
     */
    @Nested
    @DisplayName("getWeightedAverageGainLossPercentage() Tests")
    class GetWeightedAverageGainLossPercentageTests {

        @Test
        @DisplayName("Should calculate weighted average correctly")
        void getWeightedAverageGainLossPercentage_ShouldCalculateCorrectly() {
            // Arrange: Single stock for simpler verification
            when(portfolioRepository.findAll()).thenReturn(List.of(existingItem));

            Map<String, Double> currentPrices = new HashMap<>();
            currentPrices.put(SYMBOL_AAPL, 150.0); // 50% gain

            // Act
            Double weightedAvg = portfolioService.getWeightedAverageGainLossPercentage(currentPrices);

            // Assert: With single stock, weighted avg = simple percentage = 50%
            assertThat(weightedAvg).isCloseTo(50.0, within(0.001));
        }

        @Test
        @DisplayName("Should return zero when portfolio is empty")
        void getWeightedAverageGainLossPercentage_WhenEmpty_ShouldReturnZero() {
            when(portfolioRepository.findAll()).thenReturn(Collections.emptyList());

            Double weightedAvg = portfolioService.getWeightedAverageGainLossPercentage(new HashMap<>());

            assertThat(weightedAvg).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle mixed gains and losses")
        void getWeightedAverageGainLossPercentage_WithMixedGainsAndLosses_ShouldCalculateCorrectly() {
            // Arrange: Two stocks, one gaining and one losing
            PortfolioItem losingItem = new PortfolioItem(2L, SYMBOL_GOOGL, 100.0, 10);
            when(portfolioRepository.findAll()).thenReturn(List.of(existingItem, losingItem));

            Map<String, Double> currentPrices = new HashMap<>();
            currentPrices.put(SYMBOL_AAPL, 150.0);  // 50% gain, current value $1500
            currentPrices.put(SYMBOL_GOOGL, 50.0);  // -50% loss, current value $500

            // Act
            Double weightedAvg = portfolioService.getWeightedAverageGainLossPercentage(currentPrices);

            // Assert: Weighted by current value
            // AAPL: 50% * ($1500 / $2000) = 50% * 0.75 = 37.5%
            // GOOGL: -50% * ($500 / $2000) = -50% * 0.25 = -12.5%
            // Total: 37.5% - 12.5% = 25%
            assertThat(weightedAvg).isCloseTo(25.0, within(0.001));
        }
    }
}
