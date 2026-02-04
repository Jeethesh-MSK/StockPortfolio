package org.example.stockpotrfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller layer tests for StockController.
 * Uses standalone MockMvc setup with Mockito for testing the controller layer in isolation.
 * All service dependencies are mocked.
 *
 * Tests cover:
 * - GET /api/stocks/price/{symbol} - Fetching current stock price
 *
 * Each test follows the Given-When-Then pattern for clarity:
 * - Given: The preconditions and test setup
 * - When: The action being tested (HTTP request)
 * - Then: The expected outcome (HTTP response and status)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockController Tests")
class StockControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Mock the StockPriceService dependency - isolates controller from external API calls
    @Mock
    private StockPriceService stockPriceService;

    @InjectMocks
    private StockController stockController;

    @BeforeEach
    void setUp() {
        // Set up standalone MockMvc with the controller under test
        mockMvc = MockMvcBuilders.standaloneSetup(stockController).build();
    }

    // ============================================================
    // GET /api/stocks/price/{symbol} - Get Stock Price Tests
    // ============================================================
    @Nested
    @DisplayName("GET /api/stocks/price/{symbol} - Get Stock Price")
    class GetCurrentPriceTests {

        /**
         * Test: Successfully retrieves stock price for valid symbol.
         * Given: A valid stock symbol exists.
         * When: GET /api/stocks/price/{symbol} is called.
         * Then: Returns 200 OK with symbol and current price.
         */
        @Test
        @DisplayName("Should return 200 OK with price for valid symbol")
        void getCurrentPrice_ValidSymbol_ReturnsOkWithPrice() throws Exception {
            // Arrange
            String symbol = "AAPL";
            Double expectedPrice = 175.50;
            when(stockPriceService.getCurrentPrice(symbol)).thenReturn(expectedPrice);

            // Act & Assert
            mockMvc.perform(get("/api/stocks/price/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.symbol").value("AAPL"))
                    .andExpect(jsonPath("$.price").value(175.50));

            // Verify service was called exactly once with correct parameter
            verify(stockPriceService, times(1)).getCurrentPrice(symbol);
        }

        /**
         * Test: Returns price for lowercase symbol (case-insensitive).
         * Given: A lowercase stock symbol is provided.
         * When: GET /api/stocks/price/{symbol} is called.
         * Then: Returns 200 OK with uppercase symbol in response.
         */
        @Test
        @DisplayName("Should handle lowercase symbol and return uppercase in response")
        void getCurrentPrice_LowercaseSymbol_ReturnsUppercase() throws Exception {
            // Arrange
            String symbol = "aapl";
            Double expectedPrice = 175.50;
            when(stockPriceService.getCurrentPrice(symbol)).thenReturn(expectedPrice);

            // Act & Assert
            mockMvc.perform(get("/api/stocks/price/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.symbol").value("AAPL"))
                    .andExpect(jsonPath("$.price").value(175.50));
        }

        /**
         * Test: Returns 404 when stock symbol not found.
         * Given: Stock symbol does not exist or price unavailable.
         * When: GET /api/stocks/price/{symbol} is called.
         * Then: Returns 404 Not Found with error message.
         */
        @Test
        @DisplayName("Should return 404 Not Found when price not available for symbol")
        void getCurrentPrice_PriceNotFound_ReturnsNotFound() throws Exception {
            // Arrange
            String symbol = "INVALID";
            when(stockPriceService.getCurrentPrice(symbol)).thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/stocks/price/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Price not found"))
                    .andExpect(jsonPath("$.message").value(containsString("INVALID")));

            verify(stockPriceService, times(1)).getCurrentPrice(symbol);
        }

        /**
         * Test: Returns 400 when symbol contains only whitespace.
         * Given: Whitespace-only stock symbol.
         * When: GET /api/stocks/price/{symbol} is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request for whitespace-only symbol")
        void getCurrentPrice_WhitespaceSymbol_ReturnsBadRequest() throws Exception {
            // Arrange
            String symbol = "   ";

            // Act & Assert
            mockMvc.perform(get("/api/stocks/price/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid stock symbol"));
        }

        /**
         * Test: Returns 500 when service throws exception.
         * Given: StockPriceService throws an unexpected exception.
         * When: GET /api/stocks/price/{symbol} is called.
         * Then: Returns 500 Internal Server Error.
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception")
        void getCurrentPrice_ServiceException_ReturnsInternalServerError() throws Exception {
            // Arrange
            String symbol = "AAPL";
            when(stockPriceService.getCurrentPrice(anyString()))
                    .thenThrow(new RuntimeException("API connection failed"));

            // Act & Assert
            mockMvc.perform(get("/api/stocks/price/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal server error"))
                    .andExpect(jsonPath("$.message").isNotEmpty());

            verify(stockPriceService, times(1)).getCurrentPrice(symbol);
        }

        /**
         * Test: Successfully retrieves price for different stock symbols.
         * Given: Various valid stock symbols.
         * When: GET /api/stocks/price/{symbol} is called for each.
         * Then: Returns 200 OK with correct prices.
         */
        @Test
        @DisplayName("Should return correct prices for different stock symbols")
        void getCurrentPrice_MultipleSymbols_ReturnsCorrectPrices() throws Exception {
            // Test with GOOGL
            when(stockPriceService.getCurrentPrice("GOOGL")).thenReturn(2850.00);

            mockMvc.perform(get("/api/stocks/price/{symbol}", "GOOGL")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.symbol").value("GOOGL"))
                    .andExpect(jsonPath("$.price").value(2850.00));

            // Test with MSFT
            when(stockPriceService.getCurrentPrice("MSFT")).thenReturn(380.25);

            mockMvc.perform(get("/api/stocks/price/{symbol}", "MSFT")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.symbol").value("MSFT"))
                    .andExpect(jsonPath("$.price").value(380.25));

            verify(stockPriceService, times(1)).getCurrentPrice("GOOGL");
            verify(stockPriceService, times(1)).getCurrentPrice("MSFT");
        }

        /**
         * Test: Returns 200 OK with zero price for valid symbol with zero price.
         * Given: Stock has zero price (edge case).
         * When: GET /api/stocks/price/{symbol} is called.
         * Then: Returns 200 OK with price of 0.
         */
        @Test
        @DisplayName("Should return 200 OK with zero price when stock price is zero")
        void getCurrentPrice_ZeroPrice_ReturnsOkWithZero() throws Exception {
            // Arrange
            String symbol = "PENNY";
            when(stockPriceService.getCurrentPrice(symbol)).thenReturn(0.0);

            // Act & Assert
            mockMvc.perform(get("/api/stocks/price/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.symbol").value("PENNY"))
                    .andExpect(jsonPath("$.price").value(0.0));
        }

        /**
         * Test: Handles special characters in symbol.
         * Given: Stock symbol with special characters.
         * When: GET /api/stocks/price/{symbol} is called.
         * Then: Processes the request (API decides validity).
         */
        @Test
        @DisplayName("Should handle symbols with dots (e.g., BRK.B)")
        void getCurrentPrice_SymbolWithDot_HandlesCorrectly() throws Exception {
            // Arrange
            String symbol = "BRK.B";
            when(stockPriceService.getCurrentPrice(symbol)).thenReturn(350.00);

            // Act & Assert
            mockMvc.perform(get("/api/stocks/price/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.symbol").value("BRK.B"))
                    .andExpect(jsonPath("$.price").value(350.00));
        }

        /**
         * Test: Returns price with decimal precision.
         * Given: Stock price with many decimal places.
         * When: GET /api/stocks/price/{symbol} is called.
         * Then: Returns 200 OK with precise price.
         */
        @Test
        @DisplayName("Should return price with decimal precision")
        void getCurrentPrice_DecimalPrice_ReturnsWithPrecision() throws Exception {
            // Arrange
            String symbol = "AAPL";
            Double precisePrice = 175.123456;
            when(stockPriceService.getCurrentPrice(symbol)).thenReturn(precisePrice);

            // Act & Assert
            mockMvc.perform(get("/api/stocks/price/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(175.123456));
        }
    }
}
