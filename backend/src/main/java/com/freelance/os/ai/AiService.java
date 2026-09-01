package com.freelance.os.ai;

import com.freelance.os.ai.dto.AiResponse;
import com.freelance.os.common.exception.AiServiceException;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AiService {

    private final Client client;
    private final String apiKey;
    private final AiContextBuilder aiContextBuilder;
    private final AiPromptBuilder aiPromptBuilder;

    public AiService(@Value("${app.ai.gemini-api-key:}") String apiKey,
                     AiContextBuilder aiContextBuilder,
                     AiPromptBuilder aiPromptBuilder) {
        this.apiKey = apiKey;
        this.aiContextBuilder = aiContextBuilder;
        this.aiPromptBuilder = aiPromptBuilder;
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.client = Client.builder()
                    .apiKey(apiKey)
                    .build();
        } else {
            this.client = null;
        }
    }

    public AiResponse chat(String message, UUID userId) {
        String userContext = aiContextBuilder.buildContext(userId, message);
        String fullPrompt = aiPromptBuilder.buildPrompt(message, userContext);
        String text = generateResponse(fullPrompt);
        return AiResponse.builder()
                .response(text)
                .build();
    }

    public String chatWithHistory(com.freelance.os.ai.entity.ConversationMessage[] history, String message, UUID userId) {
        return chatWithHistory(java.util.Arrays.asList(history), message, userId);
    }

    public String chatWithHistory(java.util.List<com.freelance.os.ai.entity.ConversationMessage> history, String message, UUID userId) {
        String userContext = aiContextBuilder.buildContext(userId, message);
        String fullPrompt = aiPromptBuilder.buildConversationPrompt(history, message, userContext);
        return generateResponse(fullPrompt);
    }

    public String generateResponse(String fullPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty() || client == null) {
            throw new AiServiceException("Gemini API key is not configured. AI service is unavailable.");
        }

        try {
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    fullPrompt,
                    null
            );

            if (response == null || response.text() == null) {
                throw new AiServiceException("AI service returned an empty response.");
            }

            return response.text();
        } catch (AiServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiServiceException("Failed to generate AI response. Please try again later.");
        }
    }
}