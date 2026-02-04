package org.example.stockpotrfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.stockpotrfolio.service.CandleService;
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

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller layer tests for CandleController.
 * Uses standalone MockMvc setup with Mockito for testing the controller layer in isolation.
 * All service dependencies are mocked.
 *
 * Tests cover:
 * - GET /api/candles/{symbol} - Fetching candlestick chart data
 *
 * The CandleController provides historical OHLCV data for stock charting.
 * It supports different resolutions: D (daily), W (weekly), M (monthly).
 *
 * Each test follows the Given-When-Then pattern for clarity.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CandleController Tests")
class CandleControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Mock the CandleService dependency - isolates controller from external API calls
    @Mock
    private CandleService candleService;

    @InjectMocks
    private CandleController candleController;

    @BeforeEach
    void setUp() {
        // Set up standalone MockMvc with the controller under test
        mockMvc = MockMvcBuilders.standaloneSetup(candleController).build();
    }

    // ============================================================
    // GET /api/candles/{symbol} - Get Candle Data Tests
    // ============================================================
    @Nested
    @DisplayName("GET /api/candles/{symbol} - Get Candlestick Data")
    class GetCandleDataTests {

        /**
         * Creates a sample valid candle data response for testing.
         * Simulates the response format from Twelve Data API.
         */
        private Map<String, Object> createValidCandleResponse(String symbol) {
            Map<String, Object> response = new HashMap<>();
            response.put("s", "ok");
            response.put("symbol", symbol.toUpperCase());

            // OHLCV arrays
            response.put("o", Arrays.asList(150.0, 152.0, 155.0)); // Open prices
            response.put("h", Arrays.asList(155.0, 157.0, 160.0)); // High prices
            response.put("l", Arrays.asList(148.0, 150.0, 153.0)); // Low prices
            response.put("c", Arrays.asList(153.0, 156.0, 158.0)); // Close prices
            response.put("v", Arrays.asList(1000000L, 1200000L, 1100000L)); // Volumes
            response.put("t", Arrays.asList(1704067200L, 1704153600L, 1704240000L)); // Timestamps

            return response;
        }

        /**
         * Creates an error response map.
         */
        private Map<String, Object> createErrorResponse(String code, String message) {
            Map<String, Object> error = new HashMap<>();
            error.put("s", "error");
            error.put("code", code);
            error.put("message", message);
            return error;
        }

        /**
         * Test: Successfully retrieves candle data for valid symbol with default resolution.
         * Given: A valid stock symbol exists.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 200 OK with OHLCV candlestick data.
         */
        @Test
        @DisplayName("Should return 200 OK with candle data for valid symbol")
        void getCandleData_ValidSymbol_ReturnsOkWithData() throws Exception {
            // Arrange
            String symbol = "AAPL";
            Map<String, Object> candleData = createValidCandleResponse(symbol);
            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(candleData);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.s").value("ok"))
                    .andExpect(jsonPath("$.symbol").value("AAPL"))
                    .andExpect(jsonPath("$.o").isArray())
                    .andExpect(jsonPath("$.h").isArray())
                    .andExpect(jsonPath("$.l").isArray())
                    .andExpect(jsonPath("$.c").isArray())
                    .andExpect(jsonPath("$.v").isArray())
                    .andExpect(jsonPath("$.t").isArray());

            verify(candleService, times(1)).getCandleData(eq(symbol), eq("D"), anyLong(), anyLong());
        }

        /**
         * Test: Returns candle data with custom resolution (weekly).
         * Given: Valid symbol and resolution W (weekly).
         * When: GET /api/candles/{symbol}?resolution=W is called.
         * Then: Returns 200 OK with weekly candle data.
         */
        @Test
        @DisplayName("Should return 200 OK with weekly candle data when resolution=W")
        void getCandleData_WeeklyResolution_ReturnsOkWithData() throws Exception {
            // Arrange
            String symbol = "GOOGL";
            Map<String, Object> candleData = createValidCandleResponse(symbol);
            when(candleService.getCandleData(eq(symbol), eq("W"), anyLong(), anyLong()))
                    .thenReturn(candleData);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .param("resolution", "W")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.s").value("ok"));

            verify(candleService, times(1)).getCandleData(eq(symbol), eq("W"), anyLong(), anyLong());
        }

        /**
         * Test: Returns candle data with monthly resolution.
         * Given: Valid symbol and resolution M (monthly).
         * When: GET /api/candles/{symbol}?resolution=M is called.
         * Then: Returns 200 OK with monthly candle data.
         */
        @Test
        @DisplayName("Should return 200 OK with monthly candle data when resolution=M")
        void getCandleData_MonthlyResolution_ReturnsOkWithData() throws Exception {
            // Arrange
            String symbol = "MSFT";
            Map<String, Object> candleData = createValidCandleResponse(symbol);
            when(candleService.getCandleData(eq(symbol), eq("M"), anyLong(), anyLong()))
                    .thenReturn(candleData);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .param("resolution", "M")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.s").value("ok"));

            verify(candleService, times(1)).getCandleData(eq(symbol), eq("M"), anyLong(), anyLong());
        }

        /**
         * Test: Returns 400 when resolution is invalid.
         * Given: Invalid resolution parameter (not D, W, or M).
         * When: GET /api/candles/{symbol}?resolution=X is called.
         * Then: Returns 400 Bad Request with validation error.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when resolution is invalid")
        void getCandleData_InvalidResolution_ReturnsBadRequest() throws Exception {
            // Arrange
            String symbol = "AAPL";

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .param("resolution", "X")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid resolution"))
                    .andExpect(jsonPath("$.message").value(containsString("D (daily)")));
        }

        /**
         * Test: Returns 400 when symbol is whitespace only.
         * Given: Symbol path variable contains only whitespace.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 400 Bad Request.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when symbol is whitespace")
        void getCandleData_WhitespaceSymbol_ReturnsBadRequest() throws Exception {
            // Arrange
            String symbol = "   ";

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid symbol"));
        }

        /**
         * Test: Returns 404 when no candle data found.
         * Given: Stock symbol has no historical data.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 404 Not Found.
         */
        @Test
        @DisplayName("Should return 404 Not Found when no candle data available")
        void getCandleData_NoData_ReturnsNotFound() throws Exception {
            // Arrange
            String symbol = "UNKNOWN";
            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Data not found"))
                    .andExpect(jsonPath("$.message").value(containsString("UNKNOWN")));
        }

        /**
         * Test: Returns 429 when rate limit exceeded.
         * Given: Service returns rate limit error response.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 429 Too Many Requests.
         */
        @Test
        @DisplayName("Should return 429 Too Many Requests when rate limit exceeded")
        void getCandleData_RateLimitExceeded_ReturnsTooManyRequests() throws Exception {
            // Arrange
            String symbol = "AAPL";
            Map<String, Object> rateLimitError = createErrorResponse(
                    "429",
                    "Rate limit exceeded. Please wait before making more requests."
            );
            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(rateLimitError);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.error").value("Rate limit exceeded"));
        }

        /**
         * Test: Returns 503 when API key is missing.
         * Given: Service returns API key missing error.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 503 Service Unavailable.
         */
        @Test
        @DisplayName("Should return 503 Service Unavailable when API key is missing")
        void getCandleData_ApiKeyMissing_ReturnsServiceUnavailable() throws Exception {
            // Arrange
            String symbol = "AAPL";
            Map<String, Object> apiKeyError = createErrorResponse(
                    "API_KEY_MISSING",
                    "Twelve Data API key is not configured"
            );
            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(apiKeyError);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("Service unavailable"));
        }

        /**
         * Test: Returns 400 when service returns generic API error.
         * Given: Service returns error response with generic error code.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 400 Bad Request with error details.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when API returns generic error")
        void getCandleData_ApiGenericError_ReturnsBadRequest() throws Exception {
            // Arrange
            String symbol = "AAPL";
            Map<String, Object> genericError = createErrorResponse(
                    "INVALID_SYMBOL",
                    "The symbol is invalid or not supported"
            );
            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(genericError);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("INVALID_SYMBOL"));
        }

        /**
         * Test: Returns 500 when service throws exception.
         * Given: CandleService throws an unexpected exception.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 500 Internal Server Error.
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception")
        void getCandleData_ServiceException_ReturnsInternalServerError() throws Exception {
            // Arrange
            String symbol = "AAPL";
            when(candleService.getCandleData(anyString(), anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Unexpected API error"));

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Server error"))
                    .andExpect(jsonPath("$.message").isNotEmpty());

            verify(candleService, times(1)).getCandleData(eq(symbol), eq("D"), anyLong(), anyLong());
        }

        /**
         * Test: Handles custom time range parameters.
         * Given: Valid symbol with from and to timestamps.
         * When: GET /api/candles/{symbol}?from=xxx&to=yyy is called.
         * Then: Returns 200 OK and passes timestamps to service.
         */
        @Test
        @DisplayName("Should accept from and to timestamp parameters")
        void getCandleData_WithTimeRange_PassesParametersToService() throws Exception {
            // Arrange
            String symbol = "NVDA";
            Long from = 1704067200L; // Example timestamp
            Long to = 1706745600L;   // Example timestamp
            Map<String, Object> candleData = createValidCandleResponse(symbol);
            when(candleService.getCandleData(eq(symbol), eq("D"), eq(from), eq(to)))
                    .thenReturn(candleData);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .param("from", from.toString())
                    .param("to", to.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.s").value("ok"));

            verify(candleService, times(1)).getCandleData(symbol, "D", from, to);
        }

        /**
         * Test: Uses default time range when not provided.
         * Given: Valid symbol without from/to parameters.
         * When: GET /api/candles/{symbol} is called.
         * Then: Service receives default calculated timestamps.
         */
        @Test
        @DisplayName("Should use default time range when from/to not provided")
        void getCandleData_NoTimeRange_UsesDefaults() throws Exception {
            // Arrange
            String symbol = "TSLA";
            Map<String, Object> candleData = createValidCandleResponse(symbol);
            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(candleData);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify service was called with some timestamps (defaults are calculated)
            verify(candleService, times(1)).getCandleData(eq(symbol), eq("D"), anyLong(), anyLong());
        }

        /**
         * Test: Handles lowercase symbol correctly.
         * Given: Lowercase stock symbol.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 200 OK and processes request.
         */
        @Test
        @DisplayName("Should handle lowercase symbol correctly")
        void getCandleData_LowercaseSymbol_ProcessesCorrectly() throws Exception {
            // Arrange
            String symbol = "aapl";
            Map<String, Object> candleData = createValidCandleResponse("AAPL");
            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(candleData);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.symbol").value("AAPL"));
        }


        /**
         * Test: Error response without code field.
         * Given: Service returns error without code field.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 400 with default error code.
         */
        @Test
        @DisplayName("Should handle error response without code field")
        void getCandleData_ErrorWithoutCode_ReturnsBadRequestWithDefault() throws Exception {
            // Arrange
            String symbol = "AAPL";
            Map<String, Object> errorWithoutCode = new HashMap<>();
            errorWithoutCode.put("s", "error");
            errorWithoutCode.put("message", "Something went wrong");
            // Note: no "code" field

            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(errorWithoutCode);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("API Error"));
        }

        /**
         * Test: Error response without message field.
         * Given: Service returns error without message field.
         * When: GET /api/candles/{symbol} is called.
         * Then: Returns 400 with default message.
         */
        @Test
        @DisplayName("Should handle error response without message field")
        void getCandleData_ErrorWithoutMessage_ReturnsBadRequestWithDefault() throws Exception {
            // Arrange
            String symbol = "AAPL";
            Map<String, Object> errorWithoutMessage = new HashMap<>();
            errorWithoutMessage.put("s", "error");
            errorWithoutMessage.put("code", "SOME_ERROR");
            // Note: no "message" field

            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(errorWithoutMessage);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Failed to fetch data"));
        }

        /**
         * Test: Handles symbol with special characters.
         * Given: Symbol with special characters (e.g., BRK.B).
         * When: GET /api/candles/{symbol} is called.
         * Then: Passes symbol correctly to service.
         */
        @Test
        @DisplayName("Should handle symbol with special characters like dots")
        void getCandleData_SymbolWithDot_ProcessesCorrectly() throws Exception {
            // Arrange
            String symbol = "BRK.B";
            Map<String, Object> candleData = createValidCandleResponse(symbol);
            when(candleService.getCandleData(eq(symbol), eq("D"), anyLong(), anyLong()))
                    .thenReturn(candleData);

            // Act & Assert
            mockMvc.perform(get("/api/candles/{symbol}", symbol)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(candleService, times(1)).getCandleData(eq(symbol), eq("D"), anyLong(), anyLong());
        }
    }
}
