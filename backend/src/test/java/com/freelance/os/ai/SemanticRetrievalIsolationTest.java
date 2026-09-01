package com.freelance.os.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.os.ai.entity.VectorEmbedding;
import com.freelance.os.ai.repository.VectorEmbeddingRepository;
import com.freelance.os.ai.service.EmbeddingService;
import com.freelance.os.ai.service.VectorSearchService;
import com.freelance.os.user.Role;
import com.freelance.os.user.User;
import com.freelance.os.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SemanticRetrievalIsolationTest {

    @Autowired
    private VectorEmbeddingRepository vectorEmbeddingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private EmbeddingService embeddingService;
    private VectorSearchService vectorSearchService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(null, objectMapper);
        vectorSearchService = new VectorSearchService(vectorEmbeddingRepository, embeddingService);

        userA = userRepository.save(User.builder()
                .email("usera_" + UUID.randomUUID() + "@test.com")
                .fullName("User A")
                .passwordHash("hash")
                .role(Role.ROLE_FREELANCER)
                .build());

        userB = userRepository.save(User.builder()
                .email("userb_" + UUID.randomUUID() + "@test.com")
                .fullName("User B")
                .passwordHash("hash")
                .role(Role.ROLE_FREELANCER)
                .build());

        // Create Embedding for User A
        float[] vectorA = embeddingService.generateEmbedding("Secret Financial Audit Project for Acme Corp");
        vectorEmbeddingRepository.save(VectorEmbedding.builder()
                .user(userA)
                .entityType("PROJECT")
                .entityId(UUID.randomUUID())
                .content("Secret Financial Audit Project for Acme Corp")
                .embeddingJson(embeddingService.serializeVector(vectorA))
                .build());

        // Create Embedding for User B
        float[] vectorB = embeddingService.generateEmbedding("Confidential Health App Project for Beta Health");
        vectorEmbeddingRepository.save(VectorEmbedding.builder()
                .user(userB)
                .entityType("PROJECT")
                .entityId(UUID.randomUUID())
                .content("Confidential Health App Project for Beta Health")
                .embeddingJson(embeddingService.serializeVector(vectorB))
                .build());
    }

    @Test
    @DisplayName("Strict Isolation Test: User A can NEVER retrieve User B embeddings or data")
    void search_shouldNeverExposeUserBDataToUserA() {
        List<VectorSearchService.ScoredChunk> userAResults = vectorSearchService.search(
                userA.getId(), "financial audit health app confidential secret", 10, 0.001f
        );

        assertFalse(userAResults.isEmpty());
        for (VectorSearchService.ScoredChunk chunk : userAResults) {
            assertEquals(userA.getId(), chunk.embedding().getUser().getId());
            assertTrue(chunk.embedding().getContent().contains("Acme Corp"));
            assertFalse(chunk.embedding().getContent().contains("Beta Health"));
        }

        String userAContext = vectorSearchService.buildSemanticContext(userA.getId(), "financial audit project acme");
        assertTrue(userAContext.contains("Acme Corp"));
        assertFalse(userAContext.contains("Beta Health"));
    }

    @Test
    @DisplayName("Strict Isolation Test: User B can NEVER retrieve User A embeddings or data")
    void search_shouldNeverExposeUserADataToUserB() {
        List<VectorSearchService.ScoredChunk> userBResults = vectorSearchService.search(
                userB.getId(), "financial audit health app confidential secret", 10, 0.001f
        );

        assertFalse(userBResults.isEmpty());
        for (VectorSearchService.ScoredChunk chunk : userBResults) {
            assertEquals(userB.getId(), chunk.embedding().getUser().getId());
            assertTrue(chunk.embedding().getContent().contains("Beta Health"));
            assertFalse(chunk.embedding().getContent().contains("Acme Corp"));
        }

        String userBContext = vectorSearchService.buildSemanticContext(userB.getId(), "confidential health app beta");
        assertTrue(userBContext.contains("Beta Health"));
        assertFalse(userBContext.contains("Acme Corp"));
    }
}
