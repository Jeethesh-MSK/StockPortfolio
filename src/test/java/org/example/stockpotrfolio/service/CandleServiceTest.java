package org.example.stockpotrfolio.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CandleService.
 *
 * Tests cover fetching candlestick/OHLCV data with various scenarios:
 * - Successful data retrieval
 * - Invalid input handling
 * - API key configuration checks
 * - Error response handling
 *
 * Uses Mockito to mock TwelveDataService dependency to avoid actual HTTP calls
 * during unit testing and to simulate various API responses.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CandleService Unit Tests")
class CandleServiceTest {

    /**
     * Mock of TwelveDataService - we mock this to test CandleService business logic
     * in isolation without making actual API calls.
     */
    @Mock
    private TwelveDataService twelveDataService;

    /**
     * The service under test. Mockito will inject the mocked TwelveDataService.
     */
    @InjectMocks
    private CandleService candleService;

    // Test fixtures
    private static final String SYMBOL_AAPL = "AAPL";
    private static final String RESOLUTION_DAILY = "D";
    private static final String RESOLUTION_WEEKLY = "W";
    private static final String RESOLUTION_MONTHLY = "M";
    private static final long DEFAULT_FROM = 1704067200L; // Example timestamp
    private static final long DEFAULT_TO = 1706745600L;   // Example timestamp

    /**
     * Tests for getCandleData(symbol, resolution, from, to) method.
     * The primary method for fetching candlestick data.
     */
    @Nested
    @DisplayName("getCandleData(symbol, resolution, from, to) Tests")
    class GetCandleDataWithTimestampsTests {

        @Test
        @DisplayName("Should return candle data when API returns valid response")
        void getCandleData_WithValidResponse_ShouldReturnData() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);

            Map<String, Object> mockResponse = createSuccessfulCandleResponse();
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(mockResponse);

            // Act
            Map<String, Object> result = candleService.getCandleData(SYMBOL_AAPL, RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("ok");
            assertThat(result.get("symbol")).isEqualTo(SYMBOL_AAPL);

            verify(twelveDataService).isApiKeyConfigured();
            verify(twelveDataService).getTimeSeries(eq(SYMBOL_AAPL), eq("1day"), anyInt());
        }

        @Test
        @DisplayName("Should return error response when symbol is null")
        void getCandleData_WithNullSymbol_ShouldReturnErrorResponse() {
            // Act
            Map<String, Object> result = candleService.getCandleData(null, RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("error");
            assertThat(result.get("code")).isEqualTo("INVALID_SYMBOL");
            assertThat(result.get("message")).asString().contains("cannot be null or empty");

            // Should not interact with API service
            verify(twelveDataService, never()).isApiKeyConfigured();
            verify(twelveDataService, never()).getTimeSeries(anyString(), anyString(), anyInt());
        }

        @Test
        @DisplayName("Should return error response when symbol is empty")
        void getCandleData_WithEmptySymbol_ShouldReturnErrorResponse() {
            Map<String, Object> result = candleService.getCandleData("", RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("error");
            assertThat(result.get("code")).isEqualTo("INVALID_SYMBOL");
        }

        @Test
        @DisplayName("Should return error response when symbol contains only whitespace")
        void getCandleData_WithWhitespaceSymbol_ShouldReturnErrorResponse() {
            Map<String, Object> result = candleService.getCandleData("   ", RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("error");
            assertThat(result.get("code")).isEqualTo("INVALID_SYMBOL");
        }

        @Test
        @DisplayName("Should use default resolution 'D' when resolution is null")
        void getCandleData_WithNullResolution_ShouldUseDefaultDaily() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(createSuccessfulCandleResponse());

            // Act
            candleService.getCandleData(SYMBOL_AAPL, null, DEFAULT_FROM, DEFAULT_TO);

            // Assert: Should use "1day" interval (mapped from default "D")
            verify(twelveDataService).getTimeSeries(eq(SYMBOL_AAPL), eq("1day"), anyInt());
        }

        @Test
        @DisplayName("Should use default resolution 'D' when resolution is empty")
        void getCandleData_WithEmptyResolution_ShouldUseDefaultDaily() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(createSuccessfulCandleResponse());

            // Act
            candleService.getCandleData(SYMBOL_AAPL, "", DEFAULT_FROM, DEFAULT_TO);

            // Assert
            verify(twelveDataService).getTimeSeries(eq(SYMBOL_AAPL), eq("1day"), anyInt());
        }

        @Test
        @DisplayName("Should return error response when API key is not configured")
        void getCandleData_WithMissingApiKey_ShouldReturnErrorResponse() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(false);

            // Act
            Map<String, Object> result = candleService.getCandleData(SYMBOL_AAPL, RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("error");
            assertThat(result.get("code")).isEqualTo("API_KEY_MISSING");
            assertThat(result.get("message")).asString().contains("not configured");

            // Should not make API call
            verify(twelveDataService, never()).getTimeSeries(anyString(), anyString(), anyInt());
        }

        @Test
        @DisplayName("Should return error response when API returns null")
        void getCandleData_WhenApiReturnsNull_ShouldReturnErrorResponse() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(null);

            // Act
            Map<String, Object> result = candleService.getCandleData(SYMBOL_AAPL, RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("error");
            assertThat(result.get("code")).isEqualTo("API_ERROR");
            assertThat(result.get("message")).asString().contains("Failed to fetch historical data");
        }

        @Test
        @DisplayName("Should return API error response when API returns error status")
        void getCandleData_WhenApiReturnsError_ShouldReturnErrorResponse() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("s", "error");
            errorResponse.put("code", "RATE_LIMIT");
            errorResponse.put("message", "Too many requests");

            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(errorResponse);

            // Act
            Map<String, Object> result = candleService.getCandleData(SYMBOL_AAPL, RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("error");
            assertThat(result.get("code")).isEqualTo("RATE_LIMIT");
            assertThat(result.get("message")).isEqualTo("Too many requests");
        }

        @Test
        @DisplayName("Should normalize symbol to uppercase")
        void getCandleData_WithLowercaseSymbol_ShouldNormalizeToUppercase() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(createSuccessfulCandleResponse());

            // Act
            candleService.getCandleData("aapl", RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            verify(twelveDataService).getTimeSeries(eq("AAPL"), anyString(), anyInt());
        }

        @Test
        @DisplayName("Should trim whitespace from symbol")
        void getCandleData_WithWhitespacePaddedSymbol_ShouldTrimSymbol() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(createSuccessfulCandleResponse());

            // Act
            candleService.getCandleData("  AAPL  ", RESOLUTION_DAILY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            verify(twelveDataService).getTimeSeries(eq("AAPL"), anyString(), anyInt());
        }

        @Test
        @DisplayName("Should map resolution 'W' to '1week' interval")
        void getCandleData_WithWeeklyResolution_ShouldMapToCorrectInterval() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(createSuccessfulCandleResponse());

            // Act
            candleService.getCandleData(SYMBOL_AAPL, RESOLUTION_WEEKLY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            verify(twelveDataService).getTimeSeries(eq(SYMBOL_AAPL), eq("1week"), anyInt());
        }

        @Test
        @DisplayName("Should map resolution 'M' to '1month' interval")
        void getCandleData_WithMonthlyResolution_ShouldMapToCorrectInterval() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(createSuccessfulCandleResponse());

            // Act
            candleService.getCandleData(SYMBOL_AAPL, RESOLUTION_MONTHLY, DEFAULT_FROM, DEFAULT_TO);

            // Assert
            verify(twelveDataService).getTimeSeries(eq(SYMBOL_AAPL), eq("1month"), anyInt());
        }
    }

    /**
     * Tests for getCandleData(symbol, resolution) overloaded method.
     * Uses default time range.
     */
    @Nested
    @DisplayName("getCandleData(symbol, resolution) Overloaded Method Tests")
    class GetCandleDataWithDefaultTimeRangeTests {

        @Test
        @DisplayName("Should call full method with calculated default timestamps")
        void getCandleData_WithDefaultTimeRange_ShouldDelegateCorrectly() {
            // Arrange
            when(twelveDataService.isApiKeyConfigured()).thenReturn(true);
            when(twelveDataService.getTimeSeries(anyString(), anyString(), anyInt()))
                    .thenReturn(createSuccessfulCandleResponse());

            // Act
            Map<String, Object> result = candleService.getCandleData(SYMBOL_AAPL, RESOLUTION_DAILY);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("ok");
            verify(twelveDataService).getTimeSeries(eq(SYMBOL_AAPL), eq("1day"), anyInt());
        }

        @Test
        @DisplayName("Should return error for null symbol using overloaded method")
        void getCandleData_Overloaded_WithNullSymbol_ShouldReturnError() {
            Map<String, Object> result = candleService.getCandleData(null, RESOLUTION_DAILY);

            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("error");
            assertThat(result.get("code")).isEqualTo("INVALID_SYMBOL");
        }
    }

    /**
     * Helper method to create a successful candle response for mocking.
     */
    private Map<String, Object> createSuccessfulCandleResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("s", "ok");
        response.put("symbol", SYMBOL_AAPL);
        response.put("t", new long[]{1704067200L, 1704153600L, 1704240000L});
        response.put("o", new double[]{175.0, 176.0, 177.0});
        response.put("h", new double[]{176.5, 177.5, 178.5});
        response.put("l", new double[]{174.0, 175.0, 176.0});
        response.put("c", new double[]{176.0, 177.0, 178.0});
        response.put("v", new long[]{1000000L, 1100000L, 1200000L});
        return response;
    }
}
