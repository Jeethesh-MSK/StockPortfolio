package org.example.stockpotrfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import org.example.stockpotrfolio.dto.ErrorResponse;
import org.example.stockpotrfolio.dto.PortfolioSummary;
import org.example.stockpotrfolio.entity.PortfolioItem;
import org.example.stockpotrfolio.exception.DatabaseException;
import org.example.stockpotrfolio.exception.InsufficientSharesException;
import org.example.stockpotrfolio.exception.ResourceNotFoundException;
import org.example.stockpotrfolio.exception.ValidationException;
import org.example.stockpotrfolio.service.PortfolioService;
import org.example.stockpotrfolio.service.StockPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for portfolio-related operations.
 * Provides endpoints to view and manage the user's stock portfolio.
 */
@RestController
@RequestMapping("/api/portfolio")
@Tag(name = "Portfolio", description = "Portfolio management and viewing endpoints")
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final StockPriceService stockPriceService;

    @Autowired
    public PortfolioController(PortfolioService portfolioService, StockPriceService stockPriceService) {
        this.portfolioService = portfolioService;
        this.stockPriceService = stockPriceService;
    }

    /**
     * Get the complete portfolio with live price information.
     * Fetches all portfolio items from the database and enriches them with live prices
     * from the Finnhub API. Calculates profit/loss for each holding.
     *
     * @return ResponseEntity with a list of PortfolioSummary objects or error message
     */
    @GetMapping
    @Operation(summary = "Get complete portfolio with live prices",
            description = "Fetches all portfolio items and enriches them with live stock prices from Finnhub API. " +
                    "Calculates profit/loss percentage for each holding.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved portfolio with live prices",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PortfolioSummary.class))),
            @ApiResponse(responseCode = "500",
                    description = "Error fetching portfolio or live prices",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> getPortfolio() {
        log.info("Received request to fetch complete portfolio with live prices");

        try {
            // Fetch all portfolio items from the database
            List<PortfolioItem> portfolioItems = portfolioService.getPortfolio();
            log.info("Retrieved {} portfolio items from database", portfolioItems.size());

            // Map each portfolio item to a PortfolioSummary with live prices
            List<PortfolioSummary> portfolioSummaries = portfolioItems.stream()
                    .map(item -> {
                        log.debug("Processing portfolio item: {}", item.getSymbol());

                        // Fetch the current live price for this stock
                        Double currentLivePrice = stockPriceService.getCurrentPrice(item.getSymbol());

                        // If we couldn't fetch the live price, use the average buy price as fallback
                        if (currentLivePrice == null) {
                            log.warn("Could not fetch live price for symbol: {}, using average buy price as fallback",
                                    item.getSymbol());
                            currentLivePrice = item.getAverageBuyPrice();
                        }

                        // Create a PortfolioSummary with calculated profit/loss
                        // Formula: (CurrentPrice - AvgPrice) / AvgPrice * 100
                        return PortfolioSummary.from(
                                item.getSymbol(),
                                item.getQuantity(),
                                item.getAverageBuyPrice(),
                                currentLivePrice
                        );
                    })
                    .collect(Collectors.toList());

            log.info("Successfully generated {} portfolio summaries with live prices", portfolioSummaries.size());
            return ResponseEntity.ok(new PortfolioResponse(portfolioSummaries));

        } catch (DatabaseException e) {
            log.error("Database error fetching portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database Error",
                            "Unable to retrieve portfolio. Please try again later."));
        } catch (NullPointerException e) {
            log.error("Null pointer error fetching portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Error",
                            "An unexpected error occurred while fetching the portfolio"));
        } catch (Exception e) {
            log.error("Error fetching portfolio with live prices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Portfolio fetch error",
                            "An error occurred while fetching the portfolio with live prices"));
        }
    }

    /**
     * Response class for successful portfolio queries.
     */
    @Getter
    @Setter
    public static class PortfolioResponse {
        private List<PortfolioSummary> portfolio;
        private Integer totalItems;
        private Double totalInvested;
        private Double currentTotalValue;
        private Double totalGainLoss;
        private Double totalGainLossPercentage;

        public PortfolioResponse(List<PortfolioSummary> portfolio) {
            this.portfolio = portfolio;
            this.totalItems = portfolio.size();

            // Calculate aggregate portfolio metrics
            this.totalInvested = portfolio.stream()
                    .mapToDouble(PortfolioSummary::getTotalInvested)
                    .sum();

            this.currentTotalValue = portfolio.stream()
                    .mapToDouble(PortfolioSummary::getCurrentTotalValue)
                    .sum();

            this.totalGainLoss = portfolio.stream()
                    .mapToDouble(PortfolioSummary::getAbsoluteProfitOrLoss)
                    .sum();

            // Calculate weighted average percentage
            if (totalInvested > 0) {
                this.totalGainLossPercentage = (totalGainLoss / totalInvested) * 100;
            } else {
                this.totalGainLossPercentage = 0.0;
            }
        }
    }

    /**
     * Buy stocks and add them to the portfolio.
     * If the stock already exists, updates the average buy price using weighted average.
     *
     * @param request the buy request containing symbol, quantity, and price
     * @return ResponseEntity with the updated portfolio item or error message
     */
    @PostMapping("/buy")
    @Operation(summary = "Buy stocks",
            description = "Add stocks to the portfolio or update existing holdings with weighted average price calculation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Stock purchase successful",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input parameters",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Error processing purchase",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> buyStock(@Valid @RequestBody TransactionRequest request) {
        log.info("Received buy request - Symbol: {}, Quantity: {}, Price: ${}",
                request.getSymbol(), request.getQuantity(), request.getPrice());

        try {
            // Validate request
            if (request.getSymbol() == null || request.getSymbol().trim().isEmpty()) {
                throw new ValidationException("Stock symbol is required");
            }

            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be greater than 0");
            }

            if (request.getPrice() == null || request.getPrice() <= 0) {
                throw new ValidationException("Price must be greater than 0");
            }

            // Process the buy transaction
            PortfolioItem item = portfolioService.buyStock(
                    request.getSymbol(),
                    request.getPrice(),
                    request.getQuantity()
            );

            log.info("Buy transaction completed successfully for symbol: {}", request.getSymbol());
            return ResponseEntity.ok(new TransactionResponse(
                    "success",
                    "Stock purchase completed successfully",
                    item.getSymbol(),
                    item.getQuantity(),
                    item.getAverageBuyPrice()
            ));

        } catch (ValidationException e) {
            log.warn("Validation error in buy request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Validation Error", e.getMessage()));
        } catch (DatabaseException e) {
            log.error("Database error processing buy request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Transaction Error", "An error occurred while processing the purchase"));
        } catch (NullPointerException e) {
            log.error("Null pointer in buy request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Error", "An unexpected error occurred"));
        } catch (Exception e) {
            log.error("Error processing buy request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Transaction Error", "An error occurred while processing the purchase"));
        }
    }

    /**
     * Sell stocks from the portfolio.
     * Reduces the quantity of the stock. If all shares are sold, removes the item from portfolio.
     *
     * @param request the sell request containing symbol and quantity
     * @return ResponseEntity with transaction result or error message
     */
    @PostMapping("/sell")
    @Operation(summary = "Sell stocks",
            description = "Reduce stock quantity in the portfolio. If all shares are sold, removes the item entirely.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Stock sale successful",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input parameters or insufficient shares",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Error processing sale",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> sellStock(@Valid @RequestBody SellRequest request) {
        log.info("Received sell request - Symbol: {}, Quantity: {}",
                request.getSymbol(), request.getQuantity());

        try {
            // Validate request
            if (request.getSymbol() == null || request.getSymbol().trim().isEmpty()) {
                throw new ValidationException("Stock symbol is required");
            }

            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be greater than 0");
            }

            // Process the sell transaction
            PortfolioItem item = portfolioService.sellStock(
                    request.getSymbol(),
                    request.getQuantity()
            );

            String message;
            if (item == null) {
                message = "All shares sold. Portfolio item removed successfully";
            } else {
                message = "Shares sold successfully. Remaining quantity: " + item.getQuantity();
            }

            log.info("Sell transaction completed successfully for symbol: {}", request.getSymbol());
            return ResponseEntity.ok(new TransactionResponse(
                    "success",
                    message,
                    request.getSymbol(),
                    item != null ? item.getQuantity() : 0,
                    item != null ? item.getAverageBuyPrice() : 0.0
            ));

        } catch (ValidationException e) {
            log.warn("Validation error in sell request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Validation Error", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            log.warn("Stock not found in sell request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", e.getMessage()));
        } catch (InsufficientSharesException e) {
            log.warn("Insufficient shares in sell request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Insufficient Shares", e.getMessage()));
        } catch (DatabaseException e) {
            log.error("Database error processing sell request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Transaction Error", "An error occurred while processing the sale"));
        } catch (NullPointerException e) {
            log.error("Null pointer in sell request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Error", "An unexpected error occurred"));
        } catch (Exception e) {
            log.error("Error processing sell request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Transaction Error", "An error occurred while processing the sale"));
        }
    }

    /**
     * Request class for buy transactions.
     */
    @Getter
    @Setter
    public static class TransactionRequest {
        @jakarta.validation.constraints.NotBlank(message = "Stock symbol is required")
        private String symbol;

        @jakarta.validation.constraints.NotNull(message = "Quantity is required")
        @jakarta.validation.constraints.Positive(message = "Quantity must be greater than 0")
        private Integer quantity;

        @jakarta.validation.constraints.NotNull(message = "Price is required")
        @jakarta.validation.constraints.Positive(message = "Price must be greater than 0")
        private Double price;

        public TransactionRequest() {}

        public TransactionRequest(String symbol, Integer quantity, Double price) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.price = price;
        }
    }

    /**
     * Request class for sell transactions.
     */
    @Getter
    @Setter
    public static class SellRequest {
        @jakarta.validation.constraints.NotBlank(message = "Stock symbol is required")
        private String symbol;

        @jakarta.validation.constraints.NotNull(message = "Quantity is required")
        @jakarta.validation.constraints.Positive(message = "Quantity must be greater than 0")
        private Integer quantity;

        public SellRequest() {}

        public SellRequest(String symbol, Integer quantity) {
            this.symbol = symbol;
            this.quantity = quantity;
        }
    }

    /**
     * Response class for successful transactions.
     */
    @Getter
    @Setter
    public static class TransactionResponse {
        private String status;
        private String message;
        private String symbol;
        private Integer quantity;
        private Double averagePrice;

        public TransactionResponse(String status, String message, String symbol, Integer quantity, Double averagePrice) {
            this.status = status;
            this.message = message;
            this.symbol = symbol;
            this.quantity = quantity;
            this.averagePrice = averagePrice;
        }
    }
}
