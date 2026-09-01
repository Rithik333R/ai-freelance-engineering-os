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

    public AiService(@Value("${app.ai.gemini-api-key:}") String apiKey,
                     AiContextBuilder aiContextBuilder) {
        this.apiKey = apiKey;
        this.aiContextBuilder = aiContextBuilder;
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.client = Client.builder()
                    .apiKey(apiKey)
                    .build();
        } else {
            this.client = null;
        }
    }

    public AiResponse chat(String message, UUID userId) {
        if (apiKey == null || apiKey.trim().isEmpty() || client == null) {
            throw new AiServiceException("Gemini API key is not configured. AI service is unavailable.");
        }

        String context = aiContextBuilder.buildContext(userId);
        String prompt = context + "\n\nUser Question:\n" + message;

        try {
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );

            if (response == null || response.text() == null) {
                throw new AiServiceException("AI service returned an empty response.");
            }

            return AiResponse.builder()
                    .response(response.text())
                    .build();
        } catch (AiServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiServiceException("Failed to generate AI response. Please try again later.");
        }
    }
}