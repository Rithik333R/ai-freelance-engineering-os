package com.freelance.os.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.os.ai.entity.VectorEmbedding;
import com.freelance.os.ai.repository.VectorEmbeddingRepository;
import com.freelance.os.ai.service.EmbeddingService;
import com.freelance.os.ai.service.KnowledgeIndexingService;
import com.freelance.os.client.Client;
import com.freelance.os.project.Project;
import com.freelance.os.task.Task;
import com.freelance.os.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeIndexingServiceTest {

    @Mock
    private VectorEmbeddingRepository vectorEmbeddingRepository;

    private EmbeddingService embeddingService;
    private KnowledgeIndexingService knowledgeIndexingService;

    private User user;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(null, new ObjectMapper());
        knowledgeIndexingService = new KnowledgeIndexingService(vectorEmbeddingRepository, embeddingService);

        user = User.builder()
                .email("user@example.com")
                .fullName("Test User")
                .build();
    }

    @Test
    @DisplayName("Should index client details into vector embedding")
    void indexClient_shouldCreateEmbedding() {
        Client client = Client.builder()
                .user(user)
                .companyName("Acme Corp")
                .contactEmail("contact@acme.com")
                .notes("VIP client")
                .build();

        when(vectorEmbeddingRepository.findByUserIdAndEntityTypeAndEntityId(any(), eq("CLIENT"), any()))
                .thenReturn(Optional.empty());

        when(vectorEmbeddingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VectorEmbedding embedding = knowledgeIndexingService.indexClient(client);

        assertNotNull(embedding);
        assertEquals("CLIENT", embedding.getEntityType());
        assertTrue(embedding.getContent().contains("Acme Corp"));
        assertTrue(embedding.getContent().contains("contact@acme.com"));
        assertNotNull(embedding.getEmbeddingJson());
    }

    @Test
    @DisplayName("Should index project details into vector embedding")
    void indexProject_shouldCreateEmbedding() {
        Project project = Project.builder()
                .user(user)
                .name("Cloud Migration")
                .status("IN_PROGRESS")
                .budget(new BigDecimal("15000.00"))
                .description("Migrate legacy servers to AWS")
                .build();

        when(vectorEmbeddingRepository.findByUserIdAndEntityTypeAndEntityId(any(), eq("PROJECT"), any()))
                .thenReturn(Optional.empty());

        when(vectorEmbeddingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VectorEmbedding embedding = knowledgeIndexingService.indexProject(project);

        assertNotNull(embedding);
        assertEquals("PROJECT", embedding.getEntityType());
        assertTrue(embedding.getContent().contains("Cloud Migration"));
        assertTrue(embedding.getContent().contains("15000.00"));
    }

    @Test
    @DisplayName("Should index task details into vector embedding")
    void indexTask_shouldCreateEmbedding() {
        Task task = Task.builder()
                .title("Configure IAM roles")
                .status("TODO")
                .priority("HIGH")
                .estimatedHours(8)
                .description("Setup security roles")
                .build();

        when(vectorEmbeddingRepository.findByUserIdAndEntityTypeAndEntityId(any(), eq("TASK"), any()))
                .thenReturn(Optional.empty());

        when(vectorEmbeddingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VectorEmbedding embedding = knowledgeIndexingService.indexTask(task, "Cloud Migration", user);

        assertNotNull(embedding);
        assertEquals("TASK", embedding.getEntityType());
        assertTrue(embedding.getContent().contains("Configure IAM roles"));
        assertTrue(embedding.getContent().contains("HIGH"));
    }
}
