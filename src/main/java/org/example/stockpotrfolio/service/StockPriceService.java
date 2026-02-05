package org.example.stockpotrfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.example.stockpotrfolio.exception.ExternalApiException;
import org.example.stockpotrfolio.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service class to fetch stock prices from Finnhub API.
 * Handles REST API calls and exception management.
 */
@Service
@Slf4j
public class StockPriceService {

    private final RestTemplate restTemplate;
    private static final String FINNHUB_API_URL = "https://finnhub.io/api/v1/quote?symbol={symbol}&token={token}";
    private static final String CURRENT_PRICE_FIELD = "c";

    @Value("${finnhub.api.token:d608dl9r01qihi8os0o0d608dl9r01qihi8os0og}")
    private String apiToken;

    @Autowired
    public StockPriceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches the current stock price for the given symbol from Finnhub API.
     *
     * @param symbol the stock symbol (e.g., "AAPL", "GOOGL")
     * @return the current price as a Double, or null if the fetch fails
     */
    public Double getCurrentPrice(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Stock symbol is null or empty");
            return null;
        }

        try {
            String url = FINNHUB_API_URL
                    .replace("{symbol}", symbol.toUpperCase().trim())
                    .replace("{token}", apiToken);

            log.info("Fetching stock price for symbol: {}", symbol);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey(CURRENT_PRICE_FIELD)) {
                Object price = response.get(CURRENT_PRICE_FIELD);
                if (price instanceof Number) {
                    Double currentPrice = ((Number) price).doubleValue();
                    log.info("Successfully fetched price for {}: ${}", symbol, currentPrice);
                    return currentPrice;
                } else {
                    log.warn("Price field is not a number for symbol: {}", symbol);
                    return null;
                }
            } else {
                log.warn("Response does not contain '{}' field for symbol: {}", CURRENT_PRICE_FIELD, symbol);
                return null;
            }

        } catch (RestClientException e) {
            log.error("Failed to fetch stock price for symbol: {}. Error: {}", symbol, e.getMessage(), e);
            return null;
        } catch (NullPointerException e) {
            log.error("Null pointer while processing price for symbol: {}. Error: {}", symbol, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching stock price for symbol: {}. Error: {}", symbol, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Fetches the current stock price and throws an exception if it fails.
     * This can be useful for controllers that need explicit error handling.
     *
     * @param symbol the stock symbol
     * @return the current price as a Double
     * @throws ValidationException if symbol is invalid
     * @throws ExternalApiException if the price cannot be fetched
     */
    public Double getCurrentPriceOrThrow(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new ValidationException("Stock symbol cannot be null or empty");
        }

        try {
            String url = FINNHUB_API_URL
                    .replace("{symbol}", symbol.toUpperCase().trim())
                    .replace("{token}", apiToken);

            log.info("Fetching stock price for symbol: {}", symbol);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                throw new ExternalApiException("No response received from Finnhub API for symbol: " + symbol, "Finnhub");
            }

            if (!response.containsKey(CURRENT_PRICE_FIELD)) {
                throw new ExternalApiException("Response does not contain current price field for symbol: " + symbol, "Finnhub");
            }

            Object price = response.get(CURRENT_PRICE_FIELD);
            if (!(price instanceof Number)) {
                throw new ExternalApiException("Price field is not a valid number for symbol: " + symbol, "Finnhub");
            }

            Double currentPrice = ((Number) price).doubleValue();
            log.info("Successfully fetched price for {}: ${}", symbol, currentPrice);
            return currentPrice;

        } catch (RestClientException e) {
            log.error("REST client error while fetching stock price for symbol: {}. Error: {}", symbol, e.getMessage(), e);
            throw new ExternalApiException("Failed to connect to Finnhub API for symbol: " + symbol, "Finnhub", e);
        } catch (ValidationException | ExternalApiException e) {
            throw e; // Re-throw custom exceptions
        } catch (NullPointerException e) {
            log.error("Null pointer while fetching price for symbol: {}. Error: {}", symbol, e.getMessage(), e);
            throw new ExternalApiException("Error processing response for symbol: " + symbol, "Finnhub", e);
        } catch (Exception e) {
            log.error("Error occurred while fetching stock price for symbol: {}. Error: {}", symbol, e.getMessage(), e);
            throw new ExternalApiException("Unexpected error while fetching price for symbol: " + symbol, "Finnhub", e);
        }
    }
}
