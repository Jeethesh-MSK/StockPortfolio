package org.example.stockpotrfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TwelveDataService.
 *
 * Tests cover fetching historical OHLCV data from Twelve Data API with various scenarios:
 * - Successful data retrieval and transformation
 * - Invalid input handling
 * - API key configuration validation
 * - API error handling
 * - Data transformation logic
 *
 * Uses Mockito to mock RestTemplate dependency to avoid actual HTTP calls
 * during unit testing and to simulate various API responses.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TwelveDataService Unit Tests")
class TwelveDataServiceTest {

    /**
     * Mock of RestTemplate - we mock this to avoid actual HTTP calls
     * and to control API response behavior in tests.
     */
    @Mock
    private RestTemplate restTemplate;

    /**
     * The service under test. Mockito will inject the mocked RestTemplate.
     */
    @InjectMocks
    private TwelveDataService twelveDataService;

    // Test fixtures
    private static final String SYMBOL_AAPL = "AAPL";
    private static final String INTERVAL_DAILY = "1day";
    private static final int OUTPUT_SIZE = 90;
    private static final String VALID_API_KEY = "test_api_key_12345";

    @BeforeEach
    void setUp() {
        // Set a valid API key for most tests
        ReflectionTestUtils.setField(twelveDataService, "apiKey", VALID_API_KEY);
    }

    /**
     * Tests for getTimeSeries() method.
     * The main method for fetching OHLCV data from Twelve Data API.
     */
    @Nested
    @DisplayName("getTimeSeries() Tests")
    class GetTimeSeriesTests {

        @Test
        @DisplayName("Should return transformed data when API returns valid response")
        void getTimeSeries_WithValidResponse_ShouldReturnTransformedData() {
            // Arrange
            Map<String, Object> mockApiResponse = createValidTwelveDataResponse();
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockApiResponse);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("ok");
            assertThat(result.get("symbol")).isEqualTo(SYMBOL_AAPL);
            assertThat(result).containsKeys("t", "o", "h", "l", "c", "v");

            verify(restTemplate).getForObject(anyString(), eq(Map.class));
        }

        @Test
        @DisplayName("Should return null when symbol is null")
        void getTimeSeries_WithNullSymbol_ShouldReturnNull() {
            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(null, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
            verify(restTemplate, never()).getForObject(anyString(), any());
        }

        @Test
        @DisplayName("Should return null when symbol is empty")
        void getTimeSeries_WithEmptySymbol_ShouldReturnNull() {
            Map<String, Object> result = twelveDataService.getTimeSeries("", INTERVAL_DAILY, OUTPUT_SIZE);
            assertThat(result).isNull();
            verify(restTemplate, never()).getForObject(anyString(), any());
        }

        @Test
        @DisplayName("Should return null when symbol contains only whitespace")
        void getTimeSeries_WithWhitespaceSymbol_ShouldReturnNull() {
            Map<String, Object> result = twelveDataService.getTimeSeries("   ", INTERVAL_DAILY, OUTPUT_SIZE);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when API key is null")
        void getTimeSeries_WithNullApiKey_ShouldReturnNull() {
            // Arrange
            ReflectionTestUtils.setField(twelveDataService, "apiKey", null);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when API key is empty")
        void getTimeSeries_WithEmptyApiKey_ShouldReturnNull() {
            // Arrange
            ReflectionTestUtils.setField(twelveDataService, "apiKey", "");

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when API key is placeholder value")
        void getTimeSeries_WithPlaceholderApiKey_ShouldReturnNull() {
            // Arrange
            ReflectionTestUtils.setField(twelveDataService, "apiKey", "YOUR_TWELVEDATA_API_KEY");

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should normalize symbol to uppercase")
        void getTimeSeries_WithLowercaseSymbol_ShouldNormalizeToUppercase() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenReturn(createValidTwelveDataResponse());

            // Act
            twelveDataService.getTimeSeries("aapl", INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert: URL should contain uppercase symbol
            verify(restTemplate).getForObject(containsSubstring("symbol=AAPL"), eq(Map.class));
        }

        @Test
        @DisplayName("Should trim whitespace from symbol")
        void getTimeSeries_WithWhitespacePaddedSymbol_ShouldTrimSymbol() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenReturn(createValidTwelveDataResponse());

            // Act
            twelveDataService.getTimeSeries("  AAPL  ", INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            verify(restTemplate).getForObject(containsSubstring("symbol=AAPL"), eq(Map.class));
        }

        @Test
        @DisplayName("Should return null when API returns null response")
        void getTimeSeries_WhenApiReturnsNull_ShouldReturnNull() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return error response when API returns error code")
        void getTimeSeries_WhenApiReturnsError_ShouldReturnErrorResponse() {
            // Arrange
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 400);
            errorResponse.put("message", "Invalid symbol");

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(errorResponse);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("s")).isEqualTo("error");
            assertThat(result.get("code")).isEqualTo("400");
            assertThat(result.get("message")).isEqualTo("Invalid symbol");
        }

        @Test
        @DisplayName("Should return null when response has no values field")
        void getTimeSeries_WhenResponseMissingValues_ShouldReturnNull() {
            // Arrange
            Map<String, Object> incompleteResponse = new HashMap<>();
            incompleteResponse.put("meta", new HashMap<>());

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(incompleteResponse);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when values is null")
        void getTimeSeries_WhenValuesIsNull_ShouldReturnNull() {
            // Arrange
            Map<String, Object> responseWithNullValues = new HashMap<>();
            responseWithNullValues.put("values", null);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(responseWithNullValues);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when values is empty list")
        void getTimeSeries_WhenValuesIsEmpty_ShouldReturnNull() {
            // Arrange
            Map<String, Object> responseWithEmptyValues = new HashMap<>();
            responseWithEmptyValues.put("values", new ArrayList<>());

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(responseWithEmptyValues);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when RestClientException occurs")
        void getTimeSeries_WhenRestClientException_ShouldReturnNull() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenThrow(new RestClientException("Connection timeout"));

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when unexpected exception occurs")
        void getTimeSeries_WhenUnexpectedException_ShouldReturnNull() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should include correct query parameters in API URL")
        void getTimeSeries_ShouldBuildCorrectUrl() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenReturn(createValidTwelveDataResponse());

            // Act
            twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, 100);

            // Assert
            verify(restTemplate).getForObject(
                    argThat((String url) -> url.contains("symbol=AAPL")
                            && url.contains("interval=1day")
                            && url.contains("outputsize=100")
                            && url.contains("apikey=" + VALID_API_KEY)),
                    eq(Map.class));
        }
    }

    /**
     * Tests for isApiKeyConfigured() method.
     * Validates API key configuration status.
     */
    @Nested
    @DisplayName("isApiKeyConfigured() Tests")
    class IsApiKeyConfiguredTests {

        @Test
        @DisplayName("Should return true when API key is valid")
        void isApiKeyConfigured_WithValidKey_ShouldReturnTrue() {
            assertThat(twelveDataService.isApiKeyConfigured()).isTrue();
        }

        @Test
        @DisplayName("Should return false when API key is null")
        void isApiKeyConfigured_WithNullKey_ShouldReturnFalse() {
            ReflectionTestUtils.setField(twelveDataService, "apiKey", null);
            assertThat(twelveDataService.isApiKeyConfigured()).isFalse();
        }

        @Test
        @DisplayName("Should return false when API key is empty")
        void isApiKeyConfigured_WithEmptyKey_ShouldReturnFalse() {
            ReflectionTestUtils.setField(twelveDataService, "apiKey", "");
            assertThat(twelveDataService.isApiKeyConfigured()).isFalse();
        }

        @Test
        @DisplayName("Should return false when API key is placeholder")
        void isApiKeyConfigured_WithPlaceholderKey_ShouldReturnFalse() {
            ReflectionTestUtils.setField(twelveDataService, "apiKey", "YOUR_TWELVEDATA_API_KEY");
            assertThat(twelveDataService.isApiKeyConfigured()).isFalse();
        }
    }

    /**
     * Tests for static mapResolutionToInterval() method.
     * Maps chart resolution codes to Twelve Data interval strings.
     */
    @Nested
    @DisplayName("mapResolutionToInterval() Tests")
    class MapResolutionToIntervalTests {

        @ParameterizedTest
        @CsvSource({
                "D, 1day",
                "d, 1day",
                "W, 1week",
                "w, 1week",
                "M, 1month",
                "m, 1month"
        })
        @DisplayName("Should map resolution codes correctly")
        void mapResolutionToInterval_ShouldMapCorrectly(String resolution, String expectedInterval) {
            String result = TwelveDataService.mapResolutionToInterval(resolution);
            assertThat(result).isEqualTo(expectedInterval);
        }

        @Test
        @DisplayName("Should return 1day for null resolution")
        void mapResolutionToInterval_WithNull_ShouldReturnDaily() {
            String result = TwelveDataService.mapResolutionToInterval(null);
            assertThat(result).isEqualTo("1day");
        }

        @ParameterizedTest
        @ValueSource(strings = {"X", "Y", "INVALID", "1", ""})
        @DisplayName("Should return 1day for unknown resolution codes")
        void mapResolutionToInterval_WithUnknownCode_ShouldReturnDaily(String resolution) {
            String result = TwelveDataService.mapResolutionToInterval(resolution);
            assertThat(result).isEqualTo("1day");
        }
    }

    /**
     * Tests for static getRecommendedOutputSize() method.
     * Returns recommended data point count based on interval.
     */
    @Nested
    @DisplayName("getRecommendedOutputSize() Tests")
    class GetRecommendedOutputSizeTests {

        @Test
        @DisplayName("Should return 90 for daily interval")
        void getRecommendedOutputSize_ForDaily_ShouldReturn90() {
            int size = TwelveDataService.getRecommendedOutputSize("1day");
            assertThat(size).isEqualTo(90);
        }

        @Test
        @DisplayName("Should return 52 for weekly interval")
        void getRecommendedOutputSize_ForWeekly_ShouldReturn52() {
            int size = TwelveDataService.getRecommendedOutputSize("1week");
            assertThat(size).isEqualTo(52);
        }

        @Test
        @DisplayName("Should return 36 for monthly interval")
        void getRecommendedOutputSize_ForMonthly_ShouldReturn36() {
            int size = TwelveDataService.getRecommendedOutputSize("1month");
            assertThat(size).isEqualTo(36);
        }

        @Test
        @DisplayName("Should return 90 as default for unknown interval")
        void getRecommendedOutputSize_ForUnknown_ShouldReturn90() {
            int size = TwelveDataService.getRecommendedOutputSize("unknown");
            assertThat(size).isEqualTo(90);
        }
    }

    /**
     * Tests for data transformation logic.
     * Verifies that Twelve Data response format is correctly transformed.
     */
    @Nested
    @DisplayName("Data Transformation Tests")
    class DataTransformationTests {

        @Test
        @DisplayName("Should reverse chronological order of data")
        void getTimeSeries_ShouldReverseDataOrder() {
            // Arrange: Twelve Data returns newest first
            List<Map<String, String>> values = new ArrayList<>();
            values.add(createCandleData("2024-01-03", "103", "104", "102", "103.5", "3000"));
            values.add(createCandleData("2024-01-02", "102", "103", "101", "102.5", "2000"));
            values.add(createCandleData("2024-01-01", "101", "102", "100", "101.5", "1000"));

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("values", values);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(apiResponse);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert: Data should be oldest first
            assertThat(result).isNotNull();
            @SuppressWarnings("unchecked")
            List<Double> opens = (List<Double>) result.get("o");
            // After reversal: 101, 102, 103 (oldest to newest)
            assertThat(opens.get(0)).isEqualTo(101.0);
            assertThat(opens.get(2)).isEqualTo(103.0);
        }

        @Test
        @DisplayName("Should parse all OHLCV fields correctly")
        void getTimeSeries_ShouldParseAllFieldsCorrectly() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenReturn(createValidTwelveDataResponse());

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert: All OHLCV arrays should be present
            assertThat(result).containsKey("t"); // timestamps
            assertThat(result).containsKey("o"); // opens
            assertThat(result).containsKey("h"); // highs
            assertThat(result).containsKey("l"); // lows
            assertThat(result).containsKey("c"); // closes
            assertThat(result).containsKey("v"); // volumes

            // Verify data types
            assertThat(result.get("t")).isInstanceOf(List.class);
            assertThat(result.get("o")).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("Should handle missing/empty field values gracefully")
        void getTimeSeries_ShouldHandleMissingFieldValues() {
            // Arrange
            List<Map<String, String>> values = new ArrayList<>();
            Map<String, String> incompleteCandle = new HashMap<>();
            incompleteCandle.put("datetime", "2024-01-01");
            incompleteCandle.put("open", ""); // Empty value
            incompleteCandle.put("high", "102");
            incompleteCandle.put("low", null); // Null value
            incompleteCandle.put("close", "101.5");
            incompleteCandle.put("volume", "");
            values.add(incompleteCandle);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("values", values);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(apiResponse);

            // Act
            Map<String, Object> result = twelveDataService.getTimeSeries(SYMBOL_AAPL, INTERVAL_DAILY, OUTPUT_SIZE);

            // Assert: Should handle gracefully with default values
            assertThat(result).isNotNull();
            @SuppressWarnings("unchecked")
            List<Double> opens = (List<Double>) result.get("o");
            assertThat(opens.get(0)).isEqualTo(0.0); // Default for empty
        }
    }

    /**
     * Helper method to create a valid Twelve Data API response for mocking.
     */
    private Map<String, Object> createValidTwelveDataResponse() {
        List<Map<String, String>> values = new ArrayList<>();
        values.add(createCandleData("2024-01-03", "103", "104", "102", "103.5", "3000"));
        values.add(createCandleData("2024-01-02", "102", "103", "101", "102.5", "2000"));
        values.add(createCandleData("2024-01-01", "101", "102", "100", "101.5", "1000"));

        Map<String, Object> response = new HashMap<>();
        response.put("meta", new HashMap<String, Object>());
        response.put("values", values);
        return response;
    }

    /**
     * Helper method to create a single candle data entry.
     */
    private Map<String, String> createCandleData(String datetime, String open, String high,
                                                   String low, String close, String volume) {
        Map<String, String> candle = new HashMap<>();
        candle.put("datetime", datetime);
        candle.put("open", open);
        candle.put("high", high);
        candle.put("low", low);
        candle.put("close", close);
        candle.put("volume", volume);
        return candle;
    }

    /**
     * Helper method for URL contains matching.
     */
    private static String containsSubstring(String substring) {
        return argThat((String argument) -> argument != null && argument.contains(substring));
    }
}
