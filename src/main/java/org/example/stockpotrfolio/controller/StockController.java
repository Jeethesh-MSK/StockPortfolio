package org.example.stockpotrfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.stockpotrfolio.dto.ErrorResponse;
import org.example.stockpotrfolio.service.StockPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for stock-related operations.
 * Provides endpoints to fetch current stock prices from Finnhub API.
 */
@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stock", description = "Stock price management endpoints")
@Slf4j
public class StockController {

    private final StockPriceService stockPriceService;

    @Autowired
    public StockController(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    /**
     * Get the current price for a given stock symbol.
     *
     * @param symbol the stock symbol (e.g., "AAPL", "GOOGL")
     * @return ResponseEntity with the current price or error message
     */
    @GetMapping("/price/{symbol}")
    @Operation(summary = "Get current stock price",
            description = "Fetches the current price for a given stock symbol from Finnhub API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved stock price",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockPriceResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid stock symbol (null or empty)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404",
                    description = "Stock symbol not found or price not available",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Error fetching stock price from API",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> getCurrentPrice(
            @Parameter(description = "Stock symbol (e.g., AAPL, GOOGL)", required = true, example = "AAPL")
            @PathVariable String symbol) {

        log.info("Received request for stock price with symbol: {}", symbol);

        // Validate input
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Invalid symbol provided: null or empty");
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid stock symbol", "Stock symbol cannot be null or empty"));
        }

        try {
            // Fetch the price using the service
            Double price = stockPriceService.getCurrentPrice(symbol);

            if (price == null) {
                log.warn("Price not found for symbol: {}", symbol);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Price not found",
                                "Could not fetch price for symbol: " + symbol + ". Please verify the symbol is valid."));
            }

            log.info("Successfully retrieved price for symbol {}: ${}", symbol, price);
            return ResponseEntity.ok(new StockPriceResponse(symbol.toUpperCase(), price));

        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching price for symbol: {}", symbol, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error",
                            "An unexpected error occurred while fetching the stock price"));
        }
    }

    /**
     * Response class for successful stock price queries.
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    public static class StockPriceResponse {
        private String symbol;
        private Double price;
    }
}
