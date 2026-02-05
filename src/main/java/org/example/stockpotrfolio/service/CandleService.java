package org.example.stockpotrfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class to fetch candlestick/OHLCV data using Twelve Data API.
 * Provides historical price data for charting purposes.
 *
 * API Rate Limits (Free Tier): 8 requests/minute, 800 requests/day
 */
@Service
@Slf4j
public class CandleService {

    private final TwelveDataService twelveDataService;

    @Autowired
    public CandleService(TwelveDataService twelveDataService) {
        this.twelveDataService = twelveDataService;
    }

    /**
     * Fetches candlestick data for a stock symbol using Twelve Data API.
     * No fallback to mock data - returns proper error responses on failure.
     *
     * @param symbol     Stock symbol (e.g., "AAPL", "NVDA")
     * @param resolution Resolution: D (daily), W (weekly), M (monthly)
     * @param from       Unix timestamp for start time (not used with Twelve Data, kept for API compatibility)
     * @param to         Unix timestamp for end time (not used with Twelve Data, kept for API compatibility)
     * @return Map containing OHLCV data or error response
     */
    public Map<String, Object> getCandleData(String symbol, String resolution, long from, long to) {
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Stock symbol is null or empty");
            return createErrorResponse("INVALID_SYMBOL", "Stock symbol cannot be null or empty");
        }

        if (resolution == null || resolution.trim().isEmpty()) {
            resolution = "1M";  // Default to 1 month view (like Google Finance)
        }

        String upperSymbol = symbol.toUpperCase().trim();

        try {
            // Check if API key is configured
            if (!twelveDataService.isApiKeyConfigured()) {
                log.error("Twelve Data API key is not configured");
                return createErrorResponse("API_KEY_MISSING",
                    "Twelve Data API key is not configured. Please set twelvedata.api.key in application.properties");
            }

            // Map resolution to Twelve Data interval and get appropriate output size
            String interval = TwelveDataService.mapResolutionToInterval(resolution);
            int outputSize = TwelveDataService.getRecommendedOutputSize(resolution);  // Pass resolution, not interval

            log.info("Fetching candle data from Twelve Data for symbol: {}, interval: {}, outputSize: {}",
                    upperSymbol, interval, outputSize);

            // Fetch from Twelve Data API
            Map<String, Object> response = twelveDataService.getTimeSeries(upperSymbol, interval, outputSize);

            if (response == null) {
                log.error("Failed to fetch candle data from Twelve Data for symbol: {}", upperSymbol);
                return createErrorResponse("API_ERROR",
                    "Failed to fetch historical data for " + upperSymbol + ". Please try again later.");
            }

            // Check if response is an error
            if ("error".equals(response.get("s"))) {
                log.error("Twelve Data returned error for symbol {}: {}", upperSymbol, response.get("message"));
                return response;
            }

            log.info("Successfully fetched candle data from Twelve Data for symbol: {}", upperSymbol);
            return response;

        } catch (NullPointerException e) {
            log.error("Null pointer while fetching candle data for symbol {}: {}", upperSymbol, e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "An error occurred while processing candle data.");
        } catch (Exception e) {
            log.error("Unexpected error fetching candle data for symbol {}: {}", upperSymbol, e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Fetches candlestick data with default time range.
     * Time range is determined by Twelve Data's outputSize parameter.
     */
    public Map<String, Object> getCandleData(String symbol, String resolution) {
        long now = System.currentTimeMillis() / 1000;
        long defaultFrom = now - (90L * 24 * 60 * 60); // 90 days ago (not actually used by Twelve Data)
        return getCandleData(symbol, resolution, defaultFrom, now);
    }

    /**
     * Creates a standardized error response.
     */
    private Map<String, Object> createErrorResponse(String code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("s", "error");
        error.put("code", code);
        error.put("message", message);
        return error;
    }
}
