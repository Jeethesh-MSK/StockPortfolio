package org.example.stockpotrfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service class to fetch historical candlestick/OHLCV data from Twelve Data API.
 * Provides time series data for charting purposes.
 *
 * Twelve Data API: https://twelvedata.com/docs
 * Rate Limits (Free Tier): 8 requests/minute, 800 requests/day
 */
@Service
@Slf4j
public class TwelveDataService {

    private final RestTemplate restTemplate;
    private static final String TWELVE_DATA_TIME_SERIES_URL = "https://api.twelvedata.com/time_series";

    @Value("${twelvedata.api.key:}")
    private String apiKey;

    @Autowired
    public TwelveDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches historical time series data from Twelve Data API.
     *
     * @param symbol     Stock symbol (e.g., "AAPL", "NVDA")
     * @param interval   Twelve Data interval: 1day, 1week, 1month
     * @param outputSize Number of data points to return (max 5000 for free tier)
     * @return Map containing OHLCV data in standardized format, or null if failed
     */
    public Map<String, Object> getTimeSeries(String symbol, String interval, int outputSize) {
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Stock symbol is null or empty");
            return null;
        }

        if (apiKey == null || apiKey.isEmpty() || "YOUR_TWELVEDATA_API_KEY".equals(apiKey)) {
            log.error("Twelve Data API key is not configured. Please set twelvedata.api.key in application.properties");
            return null;
        }

        String upperSymbol = symbol.toUpperCase().trim();

        try {
            // Build URL with query parameters
            String url = String.format("%s?symbol=%s&interval=%s&outputsize=%d&apikey=%s",
                    TWELVE_DATA_TIME_SERIES_URL,
                    upperSymbol,
                    interval,
                    outputSize,
                    apiKey);

            log.info("Fetching time series data from Twelve Data for symbol: {}, interval: {}", upperSymbol, interval);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                log.warn("Null response from Twelve Data API for symbol: {}", upperSymbol);
                return null;
            }

            // Check for API errors
            if (response.containsKey("code") && response.containsKey("message")) {
                String errorCode = String.valueOf(response.get("code"));
                String errorMessage = String.valueOf(response.get("message"));
                log.error("Twelve Data API error for symbol {}: {} - {}", upperSymbol, errorCode, errorMessage);
                return createErrorResponse(errorCode, errorMessage);
            }

            // Check for valid response with values
            if (!response.containsKey("values") || response.get("values") == null) {
                log.warn("No values in Twelve Data response for symbol: {}", upperSymbol);
                return null;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, String>> values = (List<Map<String, String>>) response.get("values");

            if (values.isEmpty()) {
                log.warn("Empty values array in Twelve Data response for symbol: {}", upperSymbol);
                return null;
            }

            log.info("Successfully fetched {} data points from Twelve Data for symbol: {}", values.size(), upperSymbol);

            // Transform Twelve Data format to our standard format (matching Finnhub structure for compatibility)
            return transformToStandardFormat(values, upperSymbol);

        } catch (RestClientException e) {
            log.error("Twelve Data API call failed for symbol: {}. Error: {}", upperSymbol, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching from Twelve Data for symbol: {}. Error: {}", upperSymbol, e.getMessage());
            return null;
        }
    }

    /**
     * Transforms Twelve Data response format to our standard OHLCV format.
     * Twelve Data format: {"values": [{"datetime": "2024-01-15", "open": "180.50", ...}, ...]}
     * Standard format: {"s": "ok", "t": [...], "o": [...], "h": [...], "l": [...], "c": [...], "v": [...]}
     */
    private Map<String, Object> transformToStandardFormat(List<Map<String, String>> values, String symbol) {
        List<Long> timestamps = new ArrayList<>();
        List<Double> opens = new ArrayList<>();
        List<Double> highs = new ArrayList<>();
        List<Double> lows = new ArrayList<>();
        List<Double> closes = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();

        // Twelve Data returns data in reverse chronological order (newest first)
        // We need to reverse it for the chart (oldest first)
        Collections.reverse(values);

        for (Map<String, String> candle : values) {
            try {
                // Parse datetime to Unix timestamp
                String datetime = candle.get("datetime");
                long timestamp = parseDatetimeToUnixTimestamp(datetime);

                // Parse OHLCV values
                double open = parseDouble(candle.get("open"));
                double high = parseDouble(candle.get("high"));
                double low = parseDouble(candle.get("low"));
                double close = parseDouble(candle.get("close"));
                long volume = parseLong(candle.get("volume"));

                timestamps.add(timestamp);
                opens.add(open);
                highs.add(high);
                lows.add(low);
                closes.add(close);
                volumes.add(volume);
            } catch (Exception e) {
                log.warn("Failed to parse candle data: {}", candle, e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("s", "ok");
        result.put("symbol", symbol);
        result.put("t", timestamps);
        result.put("o", opens);
        result.put("h", highs);
        result.put("l", lows);
        result.put("c", closes);
        result.put("v", volumes);

        return result;
    }

    /**
     * Parses Twelve Data datetime string to Unix timestamp.
     * Twelve Data format: "2024-01-15" for daily, "2024-01-15 09:30:00" for intraday
     */
    private long parseDatetimeToUnixTimestamp(String datetime) {
        try {
            if (datetime.contains(" ")) {
                // Intraday format: "2024-01-15 09:30:00"
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(datetime,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return ldt.atZone(java.time.ZoneId.of("America/New_York")).toEpochSecond();
            } else {
                // Daily format: "2024-01-15"
                java.time.LocalDate ld = java.time.LocalDate.parse(datetime);
                return ld.atStartOfDay(java.time.ZoneId.of("America/New_York")).toEpochSecond();
            }
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}", datetime);
            return System.currentTimeMillis() / 1000;
        }
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        return Double.parseDouble(value);
    }

    private long parseLong(String value) {
        if (value == null || value.isEmpty()) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // Volume might be a decimal in some cases
            return (long) Double.parseDouble(value);
        }
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
     * Maps resolution (time period) to Twelve Data interval.
     *
     * Google Finance style data granularity:
     * - 1D = Intraday view with 5-minute candles (full trading day)
     * - 1W = Past week with 30-minute candles
     * - 1M = Past month with daily candles (~22 trading days)
     * - 1Y = Past year with daily candles (~252 trading days)
     * - 5Y = Past 5 years with weekly candles
     * - MAX = All available data with monthly candles
     */
    public static String mapResolutionToInterval(String resolution) {
        if (resolution == null) return "1day";
        return switch (resolution.toUpperCase()) {
            case "1D" -> "5min";     // 5-minute candles for intraday
            case "1W" -> "30min";    // 30-minute candles for past week
            case "1M" -> "1day";     // Daily candles for past month
            case "1Y" -> "1day";     // Daily candles for past year
            case "5Y" -> "1week";    // Weekly candles for 5 years
            case "MAX" -> "1month";  // Monthly candles for all time
            // Legacy support
            case "D" -> "1day";
            case "W" -> "1week";
            case "M" -> "1month";
            default -> "1day";
        };
    }

    /**
     * Gets recommended output size based on resolution (time range).
     * This determines how many data points to fetch.
     */
    public static int getRecommendedOutputSize(String resolution) {
        if (resolution == null) return 22;
        return switch (resolution.toUpperCase()) {
            case "1D" -> 78;        // ~78 x 5min candles for trading day (9:30 AM - 4:00 PM)
            case "1W" -> 65;        // ~65 x 30min candles for 5 trading days
            case "1M" -> 22;        // ~22 trading days in a month
            case "1Y" -> 252;       // ~252 trading days in a year
            case "5Y" -> 260;       // ~260 weeks in 5 years
            case "MAX" -> 120;      // 10 years of monthly data
            // Legacy support
            case "D" -> 90;
            case "W" -> 52;
            case "M" -> 36;
            default -> 22;
        };
    }

    /**
     * Checks if the API key is configured properly.
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !"YOUR_TWELVEDATA_API_KEY".equals(apiKey);
    }
}
