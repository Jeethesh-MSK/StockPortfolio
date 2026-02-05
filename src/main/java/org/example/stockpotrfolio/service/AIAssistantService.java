package org.example.stockpotrfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.stockpotrfolio.dto.AIChatRequest;
import org.example.stockpotrfolio.dto.AIChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service for interacting with OpenAI API
 * Uses GPT-4o-mini model - the most cost-effective option
 * Pricing: $0.15 per 1M input tokens, $0.60 per 1M output tokens
 */
@Service
@Slf4j
public class AIAssistantService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    // Using GPT-4o-mini - the most cost-effective model
    private static final String MODEL = "gpt-4o-mini";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String apiKey;

    public AIAssistantService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // System prompt to guide the AI for stock-related conversations
    private static final String SYSTEM_PROMPT = """
        You are a helpful and knowledgeable stock market assistant. Your role is to:
        
        1. Provide educational information about stocks, investing, and financial markets
        2. Explain financial concepts like P/E ratio, market cap, dividends, etc.
        3. Discuss general market trends and investment strategies
        4. When asked about specific stocks, provide general information about the company, 
           its business model, and factors that might affect its stock price
        5. Always remind users that you're providing educational information, not financial advice
        
        Important guidelines:
        - Be concise but informative
        - Use simple language to explain complex concepts
        - When discussing specific stocks, focus on publicly available information
        - Always include a disclaimer that this is not financial advice
        - Do not predict specific stock prices or guarantee returns
        - Encourage users to do their own research and consult financial advisors
        
        Format your responses clearly with bullet points or numbered lists when appropriate.
        Keep responses focused and relevant to the user's question.
        """;


    /**
     * Send a chat message to OpenAI and get a response
     * @param request The chat request containing conversation history
     * @return AI response
     */
    public AIChatResponse chat(AIChatRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("OpenAI API key not configured");
            return new AIChatResponse(
                "I'm sorry, but the AI service is not configured. Please set up the OpenAI API key in the application properties.",
                MODEL,
                0
            );
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Build messages array with system prompt
            List<Map<String, String>> messages = new ArrayList<>();

            // Add system prompt first
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            messages.add(systemMessage);

            // Add conversation history
            for (AIChatRequest.ChatMessage msg : request.getMessages()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 500); // Limit response length for cost control
            requestBody.put("temperature", 0.7); // Balanced creativity

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            log.info("Sending request to OpenAI API with model: {}", MODEL);

            ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());

                String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

                int totalTokens = root.path("usage")
                    .path("total_tokens")
                    .asInt(0);

                log.info("OpenAI response received. Tokens used: {}", totalTokens);

                return new AIChatResponse(content, MODEL, totalTokens);
            } else {
                log.error("Unexpected response from OpenAI: {}", response.getStatusCode());
                return new AIChatResponse(
                    "Sorry, I received an unexpected response. Please try again.",
                    MODEL,
                    0
                );
            }

        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage(), e);
            return new AIChatResponse(
                "I'm experiencing technical difficulties. Please try again later. Error: " + e.getMessage(),
                MODEL,
                0
            );
        }
    }
}
