package org.example.stockpotrfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.stockpotrfolio.entity.PortfolioItem;
import org.example.stockpotrfolio.service.PortfolioService;
import org.example.stockpotrfolio.service.StockPriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller layer tests for PortfolioController.
 * Uses standalone MockMvc setup with Mockito for testing the controller layer in isolation.
 * All service dependencies are mocked.
 *
 * Tests cover:
 * - GET /api/portfolio - Fetching portfolio with live prices
 * - POST /api/portfolio/buy - Buying stocks
 * - POST /api/portfolio/sell - Selling stocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioController Tests")
class PortfolioControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Mock the PortfolioService dependency - isolates controller from service layer
    @Mock
    private PortfolioService portfolioService;

    // Mock the StockPriceService dependency - isolates controller from external API calls
    @Mock
    private StockPriceService stockPriceService;

    @InjectMocks
    private PortfolioController portfolioController;

    private PortfolioItem testItem1;
    private PortfolioItem testItem2;

    @BeforeEach
    void setUp() {
        // Set up standalone MockMvc with the controller under test
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioController).build();

        // Set up test data before each test
        testItem1 = new PortfolioItem(1L, "AAPL", 150.0, 10);
        testItem2 = new PortfolioItem(2L, "GOOGL", 2800.0, 5);
    }

    // ============================================================
    // GET /api/portfolio - Get Portfolio Tests
    // ============================================================
    @Nested
    @DisplayName("GET /api/portfolio - Get Portfolio")
    class GetPortfolioTests {

        /**
         * Test: Successfully retrieves portfolio with live prices.
         * Given: Portfolio items exist in the database and live prices are available.
         * When: GET /api/portfolio is called.
         * Then: Returns 200 OK with portfolio data including calculated profit/loss.
         */
        @Test
        @DisplayName("Should return 200 OK with portfolio when items exist")
        void getPortfolio_WithItems_ReturnsOkWithPortfolio() throws Exception {
            // Arrange
            List<PortfolioItem> items = Arrays.asList(testItem1, testItem2);
            when(portfolioService.getPortfolio()).thenReturn(items);
            when(stockPriceService.getCurrentPrice("AAPL")).thenReturn(160.0);
            when(stockPriceService.getCurrentPrice("GOOGL")).thenReturn(2900.0);

            // Act & Assert
            mockMvc.perform(get("/api/portfolio")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolio", hasSize(2)))
                    .andExpect(jsonPath("$.portfolio[0].symbol").value("AAPL"))
                    .andExpect(jsonPath("$.portfolio[0].quantity").value(10))
                    .andExpect(jsonPath("$.portfolio[0].averageBuyPrice").value(150.0))
                    .andExpect(jsonPath("$.portfolio[0].currentLivePrice").value(160.0))
                    .andExpect(jsonPath("$.portfolio[1].symbol").value("GOOGL"))
                    .andExpect(jsonPath("$.totalItems").value(2))
                    .andExpect(jsonPath("$.totalInvested").isNumber())
                    .andExpect(jsonPath("$.currentTotalValue").isNumber());

            // Verify service interactions
            verify(portfolioService, times(1)).getPortfolio();
            verify(stockPriceService, times(1)).getCurrentPrice("AAPL");
            verify(stockPriceService, times(1)).getCurrentPrice("GOOGL");
        }

        /**
         * Test: Returns empty portfolio when no items exist.
         * Given: Portfolio is empty.
         * When: GET /api/portfolio is called.
         * Then: Returns 200 OK with empty portfolio array.
         */
        @Test
        @DisplayName("Should return 200 OK with empty portfolio when no items exist")
        void getPortfolio_Empty_ReturnsOkWithEmptyPortfolio() throws Exception {
            // Arrange
            when(portfolioService.getPortfolio()).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/portfolio")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolio", hasSize(0)))
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.totalInvested").value(0.0));

            verify(portfolioService, times(1)).getPortfolio();
        }

        /**
         * Test: Falls back to average buy price when live price is unavailable.
         * Given: Portfolio items exist but live prices cannot be fetched.
         * When: GET /api/portfolio is called.
         * Then: Returns 200 OK with currentLivePrice set to averageBuyPrice.
         */
        @Test
        @DisplayName("Should use average buy price as fallback when live price unavailable")
        void getPortfolio_LivePriceUnavailable_UsesFallback() throws Exception {
            // Arrange
            List<PortfolioItem> items = Collections.singletonList(testItem1);
            when(portfolioService.getPortfolio()).thenReturn(items);
            when(stockPriceService.getCurrentPrice("AAPL")).thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/portfolio")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolio", hasSize(1)))
                    .andExpect(jsonPath("$.portfolio[0].currentLivePrice").value(150.0))
                    .andExpect(jsonPath("$.portfolio[0].profitOrLossPercentage").value(0.0));

            verify(stockPriceService, times(1)).getCurrentPrice("AAPL");
        }

        /**
         * Test: Returns 500 when service throws exception.
         * Given: PortfolioService throws an unexpected exception.
         * When: GET /api/portfolio is called.
         * Then: Returns 500 Internal Server Error with error message.
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception")
        void getPortfolio_ServiceException_ReturnsInternalServerError() throws Exception {
            // Arrange
            when(portfolioService.getPortfolio()).thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            mockMvc.perform(get("/api/portfolio")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Portfolio fetch error"))
                    .andExpect(jsonPath("$.message").isNotEmpty());

            verify(portfolioService, times(1)).getPortfolio();
        }
    }

    // ============================================================
    // POST /api/portfolio/buy - Buy Stock Tests
    // ============================================================
    @Nested
    @DisplayName("POST /api/portfolio/buy - Buy Stock")
    class BuyStockTests {

        /**
         * Test: Successfully buys stock (new holding).
         * Given: Valid buy request with symbol, quantity, and price.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 200 OK with transaction confirmation.
         */
        @Test
        @DisplayName("Should return 200 OK when buying stock successfully")
        void buyStock_ValidRequest_ReturnsOk() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest("AAPL", 10, 150.0);
            PortfolioItem savedItem = new PortfolioItem(1L, "AAPL", 150.0, 10);

            when(portfolioService.buyStock(eq("AAPL"), eq(150.0), eq(10))).thenReturn(savedItem);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("Stock purchase completed successfully"))
                    .andExpect(jsonPath("$.symbol").value("AAPL"))
                    .andExpect(jsonPath("$.quantity").value(10))
                    .andExpect(jsonPath("$.averagePrice").value(150.0));

            verify(portfolioService, times(1)).buyStock("AAPL", 150.0, 10);
        }

        /**
         * Test: Returns 400 when symbol is null or empty.
         * Given: Buy request with null symbol.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when symbol is null")
        void buyStock_NullSymbol_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest(null, 10, 150.0);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when symbol is empty.
         * Given: Buy request with empty symbol string.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 400 Bad Request with validation error.
         * Note: Spring validation returns 400 status when @Valid fails
         */
        @Test
        @DisplayName("Should return 400 Bad Request when symbol is empty")
        void buyStock_EmptySymbol_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest("", 10, 150.0);

            // Act & Assert - Spring validation triggers MethodArgumentNotValidException
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when quantity is null.
         * Given: Buy request with null quantity.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when quantity is null")
        void buyStock_NullQuantity_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest("AAPL", null, 150.0);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when quantity is zero or negative.
         * Given: Buy request with quantity <= 0.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when quantity is zero or negative")
        void buyStock_InvalidQuantity_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest("AAPL", -5, 150.0);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when price is null.
         * Given: Buy request with null price.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when price is null")
        void buyStock_NullPrice_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest("AAPL", 10, null);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when price is zero or negative.
         * Given: Buy request with price <= 0.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when price is zero or negative")
        void buyStock_InvalidPrice_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest("AAPL", 10, -50.0);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when service throws IllegalArgumentException.
         * Given: Service throws validation exception.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 400 Bad Request.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when service throws IllegalArgumentException")
        void buyStock_ServiceValidationError_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest("AAPL", 10, 150.0);
            when(portfolioService.buyStock(anyString(), anyDouble(), anyInt()))
                    .thenThrow(new IllegalArgumentException("Invalid stock symbol"));

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.message").value("Invalid stock symbol"));
        }

        /**
         * Test: Returns 500 when service throws unexpected exception.
         * Given: Service throws RuntimeException.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 500 Internal Server Error.
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception")
        void buyStock_ServiceException_ReturnsInternalServerError() throws Exception {
            // Arrange
            PortfolioController.TransactionRequest request = new PortfolioController.TransactionRequest("AAPL", 10, 150.0);
            when(portfolioService.buyStock(anyString(), anyDouble(), anyInt()))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Transaction Error"));
        }

        /**
         * Test: Returns 400 when request body is empty.
         * Given: Empty request body.
         * When: POST /api/portfolio/buy is called.
         * Then: Returns 400 Bad Request.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when request body is empty")
        void buyStock_EmptyRequestBody_ReturnsBadRequest() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/portfolio/buy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ============================================================
    // POST /api/portfolio/sell - Sell Stock Tests
    // ============================================================
    @Nested
    @DisplayName("POST /api/portfolio/sell - Sell Stock")
    class SellStockTests {

        /**
         * Test: Successfully sells stock (partial sale).
         * Given: Valid sell request with symbol and quantity.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 200 OK with transaction confirmation.
         */
        @Test
        @DisplayName("Should return 200 OK when selling stock successfully (partial sale)")
        void sellStock_ValidRequest_ReturnsOkPartialSale() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest("AAPL", 5);
            PortfolioItem remainingItem = new PortfolioItem(1L, "AAPL", 150.0, 5);

            when(portfolioService.sellStock(eq("AAPL"), eq(5))).thenReturn(remainingItem);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value(containsString("Remaining quantity: 5")))
                    .andExpect(jsonPath("$.symbol").value("AAPL"))
                    .andExpect(jsonPath("$.quantity").value(5));

            verify(portfolioService, times(1)).sellStock("AAPL", 5);
        }

        /**
         * Test: Successfully sells all shares (complete sale).
         * Given: Valid sell request for all shares.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 200 OK with message indicating item removed.
         */
        @Test
        @DisplayName("Should return 200 OK when selling all shares (item removed)")
        void sellStock_AllSharesSold_ReturnsOkWithRemoval() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest("AAPL", 10);
            when(portfolioService.sellStock(eq("AAPL"), eq(10))).thenReturn(null);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value(containsString("removed")))
                    .andExpect(jsonPath("$.quantity").value(0));
        }

        /**
         * Test: Returns 400 when symbol is null or empty.
         * Given: Sell request with null symbol.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when symbol is null")
        void sellStock_NullSymbol_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest(null, 10);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when symbol is empty.
         * Given: Sell request with empty symbol.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 400 Bad Request with validation error.
         * Note: Spring validation returns 400 status when @Valid fails
         */
        @Test
        @DisplayName("Should return 400 Bad Request when symbol is empty")
        void sellStock_EmptySymbol_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest("", 10);

            // Act & Assert - Spring validation triggers MethodArgumentNotValidException
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when quantity is null.
         * Given: Sell request with null quantity.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when quantity is null")
        void sellStock_NullQuantity_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest("AAPL", null);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when quantity is zero or negative.
         * Given: Sell request with quantity <= 0.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when quantity is zero or negative")
        void sellStock_InvalidQuantity_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest("AAPL", 0);

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Test: Returns 400 when trying to sell more shares than owned.
         * Given: Sell request for more shares than in portfolio.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 400 Bad Request with validation error from service.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when selling more shares than owned")
        void sellStock_InsufficientShares_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest("AAPL", 100);
            when(portfolioService.sellStock(eq("AAPL"), eq(100)))
                    .thenThrow(new IllegalArgumentException("Insufficient shares. You only have 10 shares of AAPL"));

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.message").value(containsString("Insufficient shares")));
        }

        /**
         * Test: Returns 400 when stock not found in portfolio.
         * Given: Sell request for stock not in portfolio.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 400 Bad Request.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when stock not found in portfolio")
        void sellStock_StockNotFound_ReturnsBadRequest() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest("NVDA", 5);
            when(portfolioService.sellStock(eq("NVDA"), eq(5)))
                    .thenThrow(new IllegalArgumentException("Stock NVDA not found in portfolio"));

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.message").value(containsString("not found")));
        }

        /**
         * Test: Returns 500 when service throws unexpected exception.
         * Given: Service throws RuntimeException.
         * When: POST /api/portfolio/sell is called.
         * Then: Returns 500 Internal Server Error.
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception")
        void sellStock_ServiceException_ReturnsInternalServerError() throws Exception {
            // Arrange
            PortfolioController.SellRequest request = new PortfolioController.SellRequest("AAPL", 5);
            when(portfolioService.sellStock(anyString(), anyInt()))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            mockMvc.perform(post("/api/portfolio/sell")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Transaction Error"));
        }
    }
}
