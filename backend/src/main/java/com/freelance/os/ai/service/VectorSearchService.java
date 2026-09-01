package com.freelance.os.ai.service;

import com.freelance.os.ai.entity.VectorEmbedding;
import com.freelance.os.ai.repository.VectorEmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class VectorSearchService {

    private final VectorEmbeddingRepository vectorEmbeddingRepository;
    private final EmbeddingService embeddingService;

    public VectorSearchService(VectorEmbeddingRepository vectorEmbeddingRepository,
                               EmbeddingService embeddingService) {
        this.vectorEmbeddingRepository = vectorEmbeddingRepository;
        this.embeddingService = embeddingService;
    }

    public record ScoredChunk(VectorEmbedding embedding, float similarityScore) {}

    @Transactional(readOnly = true)
    public List<ScoredChunk> search(UUID userId, String query, int topK, float minThreshold) {
        if (userId == null || query == null || query.trim().isEmpty()) {
            return List.of();
        }

        float[] queryVector = embeddingService.generateEmbedding(query);
        List<VectorEmbedding> userEmbeddings = vectorEmbeddingRepository.findByUserId(userId);

        if (userEmbeddings.isEmpty()) {
            return List.of();
        }

        return userEmbeddings.stream()
                .map(emb -> {
                    float[] targetVector = embeddingService.deserializeVector(emb.getEmbeddingJson());
                    float score = embeddingService.calculateCosineSimilarity(queryVector, targetVector);
                    return new ScoredChunk(emb, score);
                })
                .filter(chunk -> chunk.similarityScore() >= minThreshold)
                .sorted(Comparator.comparingDouble(ScoredChunk::similarityScore).reversed())
                .limit(topK)
                .toList();
    }

    @Transactional(readOnly = true)
    public String buildSemanticContext(UUID userId, String query) {
        List<ScoredChunk> results = search(userId, query, 5, 0.05f);

        if (results.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("\n=== RELEVANT SEMANTIC CONTEXT ===\n");
        for (int i = 0; i < results.size(); i++) {
            ScoredChunk chunk = results.get(i);
            builder.append("[").append(i + 1).append("] ")
                    .append(chunk.embedding().getEntityType()).append(":\n")
                    .append(chunk.embedding().getContent()).append("\n");
        }
        builder.append("=================================\n");

        return builder.toString();
    }
}
