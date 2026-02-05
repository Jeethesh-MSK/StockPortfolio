package org.example.stockpotrfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.stockpotrfolio.dto.AIChatRequest;
import org.example.stockpotrfolio.dto.AIChatResponse;
import org.example.stockpotrfolio.service.AIAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI Assistant chat functionality.
 * Provides endpoints to interact with the OpenAI-powered stock assistant.
 * Uses GPT-4o-mini model for cost efficiency.
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Assistant", description = "AI-powered stock assistant endpoints")
@Slf4j
public class AIController {

    private final AIAssistantService aiAssistantService;

    @Autowired
    public AIController(AIAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * Send a chat message to the AI assistant and receive a response.
     *
     * @param request the chat request containing conversation history
     * @return ResponseEntity with the AI response
     */
    @PostMapping("/chat")
    @Operation(summary = "Chat with AI Assistant",
            description = "Send a message to the AI stock assistant and receive insights about stocks and investing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully received AI response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AIChatResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request - empty message",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500",
                    description = "Error processing AI request",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<AIChatResponse> chat(@RequestBody AIChatRequest request) {
        log.info("Received AI chat request with {} messages",
                request.getMessages() != null ? request.getMessages().size() : 0);

        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new AIChatResponse("Please provide a message.", "gpt-4o-mini", 0));
        }

        AIChatResponse response = aiAssistantService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for the AI service.
     */
    @GetMapping("/health")
    @Operation(summary = "AI Service Health Check",
            description = "Check if the AI assistant service is available")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Assistant service is running. Model: gpt-4o-mini (cost-optimized)");
    }
}
