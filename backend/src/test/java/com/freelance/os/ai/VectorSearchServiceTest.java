package com.freelance.os.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.os.ai.entity.VectorEmbedding;
import com.freelance.os.ai.repository.VectorEmbeddingRepository;
import com.freelance.os.ai.service.EmbeddingService;
import com.freelance.os.ai.service.VectorSearchService;
import com.freelance.os.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VectorSearchServiceTest {

    @Mock
    private VectorEmbeddingRepository vectorEmbeddingRepository;

    private EmbeddingService embeddingService;
    private VectorSearchService vectorSearchService;

    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(null, new ObjectMapper());
        vectorSearchService = new VectorSearchService(vectorEmbeddingRepository, embeddingService);

        userId = UUID.randomUUID();
        testUser = User.builder().email("test@example.com").fullName("Test User").build();
    }

    @Test
    @DisplayName("Should rank relevant vector embeddings by cosine similarity")
    void search_shouldRankBySimilarity() {
        float[] vectorA = embeddingService.generateEmbedding("deadline project mobile app");
        float[] vectorB = embeddingService.generateEmbedding("accounting tax records");

        VectorEmbedding emb1 = VectorEmbedding.builder()
                .user(testUser)
                .entityType("PROJECT")
                .entityId(UUID.randomUUID())
                .content("Mobile App Project deadline tomorrow")
                .embeddingJson(embeddingService.serializeVector(vectorA))
                .build();

        VectorEmbedding emb2 = VectorEmbedding.builder()
                .user(testUser)
                .entityType("CLIENT")
                .entityId(UUID.randomUUID())
                .content("Accounting tax service client")
                .embeddingJson(embeddingService.serializeVector(vectorB))
                .build();

        when(vectorEmbeddingRepository.findByUserId(userId)).thenReturn(List.of(emb1, emb2));

        List<VectorSearchService.ScoredChunk> results = vectorSearchService.search(userId, "mobile app deadline", 5, 0.01f);

        assertFalse(results.isEmpty());
        assertEquals("PROJECT", results.get(0).embedding().getEntityType());
        assertTrue(results.get(0).similarityScore() > 0.0f);
    }

    @Test
    @DisplayName("Should return empty list gracefully when user has no indexed embeddings")
    void search_shouldReturnEmptyWhenNoEmbeddings() {
        when(vectorEmbeddingRepository.findByUserId(userId)).thenReturn(List.of());

        List<VectorSearchService.ScoredChunk> results = vectorSearchService.search(userId, "anything", 5, 0.01f);
        assertTrue(results.isEmpty());

        String context = vectorSearchService.buildSemanticContext(userId, "anything");
        assertEquals("", context);
    }
}
