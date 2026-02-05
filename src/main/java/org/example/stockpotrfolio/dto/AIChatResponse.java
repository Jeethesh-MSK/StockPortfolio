package org.example.stockpotrfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for AI Chat Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {
    private String response;
    private String model;
    private int tokensUsed;
}
