package org.example.stockpotrfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service class to fetch candlestick/OHLCV data from Finnhub API.
 * Provides historical price data for charting purposes.
 * Falls back to realistic mock data if API fails.
 */
@Service
@Slf4j
public class CandleService {

    private final RestTemplate restTemplate;
    private final StockPriceService stockPriceService;
    private static final String FINNHUB_CANDLE_URL = "https://finnhub.io/api/v1/stock/candle?symbol={symbol}&resolution={resolution}&from={from}&to={to}&token={token}";

    @Value("${finnhub.api.token:d608dl9r01qihi8os0o0d608dl9r01qihi8os0og}")
    private String apiToken;

    // Fallback base prices for popular stocks (only used if API fails)
    // These should be updated periodically or removed in favor of API-only pricing
    private static final Map<String, Double> BASE_PRICES = Map.ofEntries(
            Map.entry("NVDA", 180.0),
            Map.entry("AAPL", 225.0),
            Map.entry("GOOGL", 185.0),
            Map.entry("MSFT", 410.0),
            Map.entry("AMZN", 230.0),
            Map.entry("META", 680.0),
            Map.entry("TSLA", 390.0),
            Map.entry("AMD", 115.0),
            Map.entry("INTC", 20.0),
            Map.entry("NFLX", 950.0)
    );

    @Autowired
    public CandleService(RestTemplate restTemplate, StockPriceService stockPriceService) {
        this.restTemplate = restTemplate;
        this.stockPriceService = stockPriceService;
    }

    /**
     * Fetches candlestick data for a stock symbol.
     * Falls back to mock data if the API call fails.
     */
    public Map<String, Object> getCandleData(String symbol, String resolution, long from, long to) {
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Stock symbol is null or empty");
            return null;
        }

        if (resolution == null || resolution.trim().isEmpty()) {
            resolution = "D";
        }

        String upperSymbol = symbol.toUpperCase().trim();

        // Try to fetch from Finnhub API if token is configured
        if (apiToken != null && !apiToken.isEmpty()) {
            try {
                String url = FINNHUB_CANDLE_URL
                        .replace("{symbol}", upperSymbol)
                        .replace("{resolution}", resolution)
                        .replace("{from}", String.valueOf(from))
                        .replace("{to}", String.valueOf(to))
                        .replace("{token}", apiToken);

                log.info("Fetching candle data from Finnhub for symbol: {}", upperSymbol);

                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response != null && "ok".equals(response.get("s"))) {
                    log.info("Successfully fetched candle data from Finnhub for symbol: {}", upperSymbol);
                    return response;
                }
            } catch (RestClientException e) {
                log.warn("Finnhub API call failed for symbol: {}. Falling back to mock data. Error: {}",
                        upperSymbol, e.getMessage());
            } catch (Exception e) {
                log.warn("Unexpected error fetching from Finnhub for symbol: {}. Falling back to mock data.",
                        upperSymbol);
            }
        }

        // Generate mock data as fallback
        log.info("Generating mock candle data for symbol: {}", upperSymbol);
        return generateMockCandleData(upperSymbol, resolution, from, to);
    }

    /**
     * Generates realistic mock candlestick data for demonstration purposes.
     * Fetches the current real-time price and generates realistic price history around it.
     */
    private Map<String, Object> generateMockCandleData(String symbol, String resolution, long from, long to) {
        // Try to get current real-time price from API
        Double currentRealPrice = stockPriceService.getCurrentPrice(symbol);
        double currentPrice = (currentRealPrice != null) ? currentRealPrice : BASE_PRICES.getOrDefault(symbol, 100.0);

        // Determine interval based on resolution
        long intervalSeconds = getIntervalSeconds(resolution);
        int numCandles = (int) Math.min(500, (to - from) / intervalSeconds);
        if (numCandles < 10) numCandles = 50;

        List<Long> timestamps = new ArrayList<>();
        List<Double> opens = new ArrayList<>();
        List<Double> highs = new ArrayList<>();
        List<Double> lows = new ArrayList<>();
        List<Double> closes = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();

        Random random = new Random(symbol.hashCode() + resolution.hashCode()); // Consistent random for same symbol and resolution

        // Generate realistic price history that ends at current price
        // Work backwards from current price to create historical data
        double[] priceHistory = new double[numCandles];
        priceHistory[numCandles - 1] = currentPrice;

        // Determine volatility based on resolution (longer timeframes = more total movement)
        double dailyVolatility = currentPrice * 0.02; // 2% daily volatility
        double candleVolatility = switch (resolution) {
            case "W" -> dailyVolatility * 2.5;  // Weekly: more volatility
            case "M" -> dailyVolatility * 5.0;  // Monthly: even more volatility
            default -> dailyVolatility;         // Daily
        };

        // Generate prices backwards from current price
        double price = currentPrice;
        for (int i = numCandles - 2; i >= 0; i--) {
            // Random walk backwards with slight mean reversion
            double change = (random.nextDouble() - 0.5) * candleVolatility;
            price = price - change; // Subtract because we're going backwards
            // Keep price positive and reasonable
            price = Math.max(price, currentPrice * 0.5);
            price = Math.min(price, currentPrice * 1.5);
            priceHistory[i] = price;
        }

        // Now generate OHLCV data forward
        long currentTime = to - (numCandles * intervalSeconds);

        for (int i = 0; i < numCandles; i++) {
            double targetClose = priceHistory[i];
            double open;

            if (i == 0) {
                // First candle: open near close
                open = targetClose * (1 + (random.nextDouble() - 0.5) * 0.01);
            } else {
                // Open at previous close
                open = priceHistory[i - 1];
            }

            double close = targetClose;

            // For the last candle, ensure it closes at exact real-time price
            if (i == numCandles - 1 && currentRealPrice != null) {
                close = currentRealPrice;
            }

            // Generate high and low
            double range = Math.abs(close - open) + candleVolatility * 0.3 * random.nextDouble();
            double high = Math.max(open, close) + range * random.nextDouble();
            double low = Math.min(open, close) - range * random.nextDouble();

            // Ensure low doesn't go negative
            low = Math.max(low, close * 0.95);

            long volume = (long) (1000000 + random.nextDouble() * 5000000);

            timestamps.add(currentTime);
            opens.add(Math.round(open * 100.0) / 100.0);
            highs.add(Math.round(high * 100.0) / 100.0);
            lows.add(Math.round(low * 100.0) / 100.0);
            closes.add(Math.round(close * 100.0) / 100.0);
            volumes.add(volume);

            currentTime += intervalSeconds;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("s", "ok");
        result.put("t", timestamps);
        result.put("o", opens);
        result.put("h", highs);
        result.put("l", lows);
        result.put("c", closes);
        result.put("v", volumes);

        return result;
    }

    /**
     * Returns the interval in seconds for a given resolution.
     * Only D, W, M are supported on Finnhub free tier.
     */
    private long getIntervalSeconds(String resolution) {
        return switch (resolution) {
            case "W" -> 604800;  // Weekly
            case "M" -> 2592000; // Monthly
            default -> 86400;   // Daily
        };
    }

    /**
     * Fetches candlestick data with default time range (last 30 days).
     */
    public Map<String, Object> getCandleData(String symbol, String resolution) {
        long now = System.currentTimeMillis() / 1000;
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60);
        return getCandleData(symbol, resolution, thirtyDaysAgo, now);
    }
}
