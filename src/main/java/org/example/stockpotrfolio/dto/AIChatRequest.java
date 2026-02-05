package org.example.stockpotrfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for AI Chat Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIChatRequest {
    private List<ChatMessage> messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
