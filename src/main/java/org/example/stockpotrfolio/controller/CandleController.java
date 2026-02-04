package org.example.stockpotrfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.stockpotrfolio.dto.ErrorResponse;
import org.example.stockpotrfolio.service.CandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for candlestick chart data.
 * Provides endpoints to fetch historical OHLCV data from Twelve Data API.
 *
 * Rate Limits: 8 requests/minute, 800 requests/day on free tier.
 */
@RestController
@RequestMapping("/api/candles")
@Tag(name = "Candles", description = "Candlestick chart data endpoints (powered by Twelve Data)")
@Slf4j
public class CandleController {

    private final CandleService candleService;

    @Autowired
    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }

    /**
     * Get candlestick data for a given stock symbol.
     *
     * @param symbol the stock symbol (e.g., "AAPL", "NVDA")
     * @param resolution candle resolution: D (day), W (week), M (month)
     * @param from Unix timestamp for start time (optional, for API compatibility)
     * @param to Unix timestamp for end time (optional, for API compatibility)
     * @return ResponseEntity with candle data or error message
     */
    @GetMapping("/{symbol}")
    @Operation(summary = "Get candlestick data",
            description = "Fetches historical OHLCV candlestick data for a given stock symbol from Twelve Data API. Rate limited to 8 requests/minute.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved candle data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400",
                    description = "Invalid stock symbol or parameters",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404",
                    description = "No candle data available for symbol",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "429",
                    description = "Rate limit exceeded (8 requests/minute)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Error fetching candle data from API",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> getCandleData(
            @Parameter(description = "Stock symbol (e.g., AAPL, NVDA)", required = true, example = "NVDA")
            @PathVariable String symbol,
            @Parameter(description = "Candle resolution: D (day), W (week), M (month)", example = "D")
            @RequestParam(defaultValue = "D") String resolution,
            @Parameter(description = "Start time as Unix timestamp (optional, for API compatibility)")
            @RequestParam(required = false) Long from,
            @Parameter(description = "End time as Unix timestamp (optional, for API compatibility)")
            @RequestParam(required = false) Long to) {

        log.info("Received request for candle data - Symbol: {}, Resolution: {}", symbol, resolution);

        // Validate symbol
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Invalid symbol provided: null or empty");
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid symbol", "Stock symbol cannot be null or empty"));
        }

        // Validate resolution
        if (!isValidResolution(resolution)) {
            log.warn("Invalid resolution provided: {}", resolution);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid resolution",
                            "Resolution must be one of: D (daily), W (weekly), M (monthly)"));
        }

        try {
            // Set default time range if not provided
            long now = System.currentTimeMillis() / 1000;
            if (to == null) {
                to = now;
            }
            if (from == null) {
                from = getDefaultFromTime(resolution, to);
            }

            Map<String, Object> candleData = candleService.getCandleData(symbol, resolution, from, to);

            if (candleData == null) {
                log.warn("No candle data found for symbol: {}", symbol);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Data not found",
                                "No candle data available for symbol: " + symbol.toUpperCase()));
            }

            // Check if response is an error from the service
            if ("error".equals(candleData.get("s"))) {
                String errorCode = (String) candleData.get("code");
                String errorMessage = (String) candleData.get("message");

                // Check for rate limit error
                if (errorCode != null && errorCode.contains("429")) {
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(new ErrorResponse("Rate limit exceeded", errorMessage));
                }

                // Check for API key error
                if ("API_KEY_MISSING".equals(errorCode)) {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(new ErrorResponse("Service unavailable", errorMessage));
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(errorCode != null ? errorCode : "API Error",
                                errorMessage != null ? errorMessage : "Failed to fetch data"));
            }

            log.info("Successfully retrieved candle data for symbol: {}", symbol);
            return ResponseEntity.ok(candleData);

        } catch (Exception e) {
            log.error("Error fetching candle data for symbol: {}", symbol, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error",
                            "An error occurred while fetching candle data"));
        }
    }

    /**
     * Validates the resolution parameter.
     * Supported: D (daily), W (weekly), M (monthly)
     */
    private boolean isValidResolution(String resolution) {
        if (resolution == null) return false;
        return resolution.matches("^(D|W|M)$");
    }

    /**
     * Gets default 'from' time based on resolution.
     * This is kept for API compatibility but Twelve Data uses outputSize instead.
     */
    private long getDefaultFromTime(String resolution, long to) {
        return switch (resolution) {
            case "W" -> to - (365L * 24 * 60 * 60);     // 1 year for weekly candles
            case "M" -> to - (3L * 365 * 24 * 60 * 60); // 3 years for monthly candles
            default -> to - (90L * 24 * 60 * 60);       // 90 days for daily candles
        };
    }
}
