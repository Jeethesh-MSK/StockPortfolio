package org.example.stockpotrfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StockPriceService.
 *
 * Tests cover fetching stock prices from Finnhub API with various scenarios:
 * - Successful price retrieval
 * - Invalid input handling
 * - API error handling
 * - Exception scenarios
 *
 * Uses Mockito to mock RestTemplate dependency to avoid actual HTTP calls
 * during unit testing and to simulate various API responses.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockPriceService Unit Tests")
class StockPriceServiceTest {

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
    private StockPriceService stockPriceService;

    // Test fixtures
    private static final String SYMBOL_AAPL = "AAPL";
    private static final Double CURRENT_PRICE = 175.50;

    /**
     * Tests for getCurrentPrice() method.
     * This method fetches the current price for a given stock symbol.
     */
    @Nested
    @DisplayName("getCurrentPrice() Tests")
    class GetCurrentPriceTests {

        @Test
        @DisplayName("Should return current price when API returns valid response")
        void getCurrentPrice_WithValidResponse_ShouldReturnPrice() {
            // Arrange: Create mock API response with price field 'c'
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("c", CURRENT_PRICE);
            mockResponse.put("d", 1.5);   // Change
            mockResponse.put("dp", 0.86); // Change percent
            mockResponse.put("h", 176.0); // High
            mockResponse.put("l", 174.0); // Low
            mockResponse.put("o", 175.0); // Open
            mockResponse.put("pc", 174.0); // Previous close

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act
            Double price = stockPriceService.getCurrentPrice(SYMBOL_AAPL);

            // Assert
            assertThat(price).isEqualTo(CURRENT_PRICE);
            verify(restTemplate).getForObject(anyString(), eq(Map.class));
        }

        @Test
        @DisplayName("Should return null when symbol is null")
        void getCurrentPrice_WithNullSymbol_ShouldReturnNull() {
            // Act
            Double price = stockPriceService.getCurrentPrice(null);

            // Assert
            assertThat(price).isNull();
            // Should not make any API call
            verify(restTemplate, never()).getForObject(anyString(), any());
        }

        @Test
        @DisplayName("Should return null when symbol is empty")
        void getCurrentPrice_WithEmptySymbol_ShouldReturnNull() {
            Double price = stockPriceService.getCurrentPrice("");
            assertThat(price).isNull();
            verify(restTemplate, never()).getForObject(anyString(), any());
        }

        @Test
        @DisplayName("Should return null when symbol contains only whitespace")
        void getCurrentPrice_WithWhitespaceSymbol_ShouldReturnNull() {
            Double price = stockPriceService.getCurrentPrice("   ");
            assertThat(price).isNull();
            verify(restTemplate, never()).getForObject(anyString(), any());
        }

        @Test
        @DisplayName("Should return null when API returns null response")
        void getCurrentPrice_WithNullResponse_ShouldReturnNull() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

            // Act
            Double price = stockPriceService.getCurrentPrice(SYMBOL_AAPL);

            // Assert
            assertThat(price).isNull();
        }

        @Test
        @DisplayName("Should return null when response does not contain price field")
        void getCurrentPrice_WithMissingPriceField_ShouldReturnNull() {
            // Arrange: Response without 'c' field
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("d", 1.5);
            mockResponse.put("dp", 0.86);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act
            Double price = stockPriceService.getCurrentPrice(SYMBOL_AAPL);

            // Assert
            assertThat(price).isNull();
        }

        @Test
        @DisplayName("Should return null when price field is not a number")
        void getCurrentPrice_WithNonNumericPrice_ShouldReturnNull() {
            // Arrange
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("c", "invalid_price");

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act
            Double price = stockPriceService.getCurrentPrice(SYMBOL_AAPL);

            // Assert
            assertThat(price).isNull();
        }

        @Test
        @DisplayName("Should return null when RestClientException occurs")
        void getCurrentPrice_WithRestClientException_ShouldReturnNull() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenThrow(new RestClientException("Connection failed"));

            // Act
            Double price = stockPriceService.getCurrentPrice(SYMBOL_AAPL);

            // Assert
            assertThat(price).isNull();
        }

        @Test
        @DisplayName("Should return null when unexpected exception occurs")
        void getCurrentPrice_WithUnexpectedException_ShouldReturnNull() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // Act
            Double price = stockPriceService.getCurrentPrice(SYMBOL_AAPL);

            // Assert
            assertThat(price).isNull();
        }

        @Test
        @DisplayName("Should handle Integer price value correctly")
        void getCurrentPrice_WithIntegerPrice_ShouldReturnDoubleValue() {
            // Arrange: Price returned as Integer
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("c", 175);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act
            Double price = stockPriceService.getCurrentPrice(SYMBOL_AAPL);

            // Assert
            assertThat(price).isEqualTo(175.0);
        }

        @Test
        @DisplayName("Should convert symbol to uppercase before API call")
        void getCurrentPrice_WithLowercaseSymbol_ShouldNormalizeSymbol() {
            // Arrange
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("c", CURRENT_PRICE);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act
            Double price = stockPriceService.getCurrentPrice("aapl");

            // Assert
            assertThat(price).isEqualTo(CURRENT_PRICE);
            // Verify the URL contains uppercase symbol
            verify(restTemplate).getForObject(containsSubstring("AAPL"), eq(Map.class));
        }
    }

    /**
     * Tests for getCurrentPriceOrThrow() method.
     * Similar to getCurrentPrice() but throws exceptions instead of returning null.
     */
    @Nested
    @DisplayName("getCurrentPriceOrThrow() Tests")
    class GetCurrentPriceOrThrowTests {

        @Test
        @DisplayName("Should return current price when API returns valid response")
        void getCurrentPriceOrThrow_WithValidResponse_ShouldReturnPrice() throws Exception {
            // Arrange
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("c", CURRENT_PRICE);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act
            Double price = stockPriceService.getCurrentPriceOrThrow(SYMBOL_AAPL);

            // Assert
            assertThat(price).isEqualTo(CURRENT_PRICE);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is null")
        void getCurrentPriceOrThrow_WithNullSymbol_ShouldThrowException() {
            assertThatThrownBy(() -> stockPriceService.getCurrentPriceOrThrow(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is empty")
        void getCurrentPriceOrThrow_WithEmptySymbol_ShouldThrowException() {
            assertThatThrownBy(() -> stockPriceService.getCurrentPriceOrThrow(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is whitespace only")
        void getCurrentPriceOrThrow_WithWhitespaceSymbol_ShouldThrowException() {
            assertThatThrownBy(() -> stockPriceService.getCurrentPriceOrThrow("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock symbol cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw Exception when API returns null response")
        void getCurrentPriceOrThrow_WithNullResponse_ShouldThrowException() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> stockPriceService.getCurrentPriceOrThrow(SYMBOL_AAPL))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("No response received from Finnhub API");
        }

        @Test
        @DisplayName("Should throw Exception when response missing price field")
        void getCurrentPriceOrThrow_WithMissingPriceField_ShouldThrowException() {
            // Arrange
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("d", 1.5);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act & Assert
            assertThatThrownBy(() -> stockPriceService.getCurrentPriceOrThrow(SYMBOL_AAPL))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Response does not contain current price field");
        }

        @Test
        @DisplayName("Should throw Exception when price field is not a number")
        void getCurrentPriceOrThrow_WithNonNumericPrice_ShouldThrowException() {
            // Arrange
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("c", "not_a_number");

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act & Assert
            assertThatThrownBy(() -> stockPriceService.getCurrentPriceOrThrow(SYMBOL_AAPL))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Price field is not a valid number");
        }

        @Test
        @DisplayName("Should throw Exception when RestClientException occurs")
        void getCurrentPriceOrThrow_WithRestClientException_ShouldThrowException() {
            // Arrange
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenThrow(new RestClientException("Connection timeout"));

            // Act & Assert
            assertThatThrownBy(() -> stockPriceService.getCurrentPriceOrThrow(SYMBOL_AAPL))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Failed to connect to Finnhub API");
        }

        @Test
        @DisplayName("Should handle Long price value correctly")
        void getCurrentPriceOrThrow_WithLongPrice_ShouldReturnDoubleValue() throws Exception {
            // Arrange
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("c", 175L);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act
            Double price = stockPriceService.getCurrentPriceOrThrow(SYMBOL_AAPL);

            // Assert
            assertThat(price).isEqualTo(175.0);
        }

        @Test
        @DisplayName("Should normalize symbol and trim whitespace")
        void getCurrentPriceOrThrow_WithWhitespacePaddedSymbol_ShouldNormalize() throws Exception {
            // Arrange
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("c", CURRENT_PRICE);

            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

            // Act
            Double price = stockPriceService.getCurrentPriceOrThrow("  aapl  ");

            // Assert
            assertThat(price).isEqualTo(CURRENT_PRICE);
            verify(restTemplate).getForObject(containsSubstring("AAPL"), eq(Map.class));
        }
    }

    /**
     * Helper method matcher for verifying URL contains specific string.
     */
    private static String containsSubstring(String substring) {
        return argThat((String argument) -> argument != null && argument.contains(substring));
    }
}
