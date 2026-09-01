package com.freelance.os.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.EmbedContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Service
public class EmbeddingService {

    private final Client client;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public EmbeddingService(@Value("${app.ai.gemini-api-key:}") String apiKey,
                            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.client = Client.builder()
                    .apiKey(apiKey)
                    .build();
        } else {
            this.client = null;
        }
    }

    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[128];
        }

        if (client != null && apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                EmbedContentResponse response = client.models.embedContent("text-embedding-004", text, null);
                if (response != null) {
                    List<Float> values = extractEmbeddingValues(response);
                    if (values != null && !values.isEmpty()) {
                        float[] vector = new float[values.size()];
                        for (int i = 0; i < values.size(); i++) {
                            vector[i] = values.get(i);
                        }
                        return normalize(vector);
                    }
                }
            } catch (Exception ignored) {
                // Fallback to deterministic vector if API call fails
            }
        }

        return generateFallbackEmbedding(text);
    }

    @SuppressWarnings("unchecked")
    private List<Float> extractEmbeddingValues(EmbedContentResponse response) {
        try {
            for (Method method : response.getClass().getMethods()) {
                if (method.getName().equals("embedding") && method.getParameterCount() == 0) {
                    Object embObj = method.invoke(response);
                    if (embObj != null) {
                        if (embObj instanceof java.util.Optional<?> opt) {
                            embObj = opt.orElse(null);
                        }
                        if (embObj != null) {
                            for (Method valMethod : embObj.getClass().getMethods()) {
                                if (valMethod.getName().equals("values") && valMethod.getParameterCount() == 0) {
                                    Object valObj = valMethod.invoke(embObj);
                                    if (valObj instanceof java.util.Optional<?> optVal) {
                                        valObj = optVal.orElse(null);
                                    }
                                    if (valObj instanceof List<?>) {
                                        return (List<Float>) valObj;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public String serializeVector(float[] vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            return Arrays.toString(vector);
        }
    }

    public float[] deserializeVector(String json) {
        try {
            return objectMapper.readValue(json, float[].class);
        } catch (Exception e) {
            return new float[128];
        }
    }

    public float calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length || vectorA.length == 0) {
            return 0.0f;
        }

        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0f || normB == 0.0f) {
            return 0.0f;
        }

        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private float[] generateFallbackEmbedding(String text) {
        int dim = 128;
        float[] vector = new float[dim];
        String normalized = text.toLowerCase();
        String[] words = normalized.split("\\W+");

        for (String word : words) {
            if (word.isEmpty()) continue;
            int hash = Math.abs(word.hashCode());
            int idx = hash % dim;
            vector[idx] += 1.0f;
        }

        return normalize(vector);
    }

    private float[] normalize(float[] vector) {
        float norm = 0.0f;
        for (float v : vector) {
            norm += v * v;
        }
        if (norm == 0.0f) return vector;
        float mag = (float) Math.sqrt(norm);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= mag;
        }
        return vector;
    }
}
