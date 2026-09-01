package com.freelance.os.ai.service;

import com.freelance.os.ai.entity.VectorEmbedding;
import com.freelance.os.ai.repository.VectorEmbeddingRepository;
import com.freelance.os.client.Client;
import com.freelance.os.project.Project;
import com.freelance.os.task.Task;
import com.freelance.os.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class KnowledgeIndexingService {

    private final VectorEmbeddingRepository vectorEmbeddingRepository;
    private final EmbeddingService embeddingService;

    public KnowledgeIndexingService(VectorEmbeddingRepository vectorEmbeddingRepository,
                                    EmbeddingService embeddingService) {
        this.vectorEmbeddingRepository = vectorEmbeddingRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public VectorEmbedding indexClient(Client client) {
        if (client == null || client.getUser() == null) return null;

        StringBuilder text = new StringBuilder();
        text.append("CLIENT INFORMATION:\n");
        text.append("Company Name: ").append(client.getCompanyName()).append("\n");
        if (client.getContactEmail() != null) text.append("Email: ").append(client.getContactEmail()).append("\n");
        if (client.getPhone() != null) text.append("Phone: ").append(client.getPhone()).append("\n");
        if (client.getNotes() != null) text.append("Notes: ").append(client.getNotes()).append("\n");

        return saveOrUpdateEmbedding(client.getUser(), "CLIENT", client.getId(), text.toString());
    }

    @Transactional
    public VectorEmbedding indexProject(Project project) {
        if (project == null || project.getUser() == null) return null;

        StringBuilder text = new StringBuilder();
        text.append("PROJECT DETAILS:\n");
        text.append("Project Name: ").append(project.getName()).append("\n");
        text.append("Status: ").append(project.getStatus()).append("\n");
        if (project.getBudget() != null) text.append("Budget: $").append(project.getBudget()).append("\n");
        if (project.getClient() != null) text.append("Client: ").append(project.getClient().getCompanyName()).append("\n");
        if (project.getStartDate() != null) text.append("Start Date: ").append(project.getStartDate()).append("\n");
        if (project.getTargetEndDate() != null) text.append("Target End Date / Deadline: ").append(project.getTargetEndDate()).append("\n");
        if (project.getDescription() != null) text.append("Description: ").append(project.getDescription()).append("\n");

        return saveOrUpdateEmbedding(project.getUser(), "PROJECT", project.getId(), text.toString());
    }

    @Transactional
    public VectorEmbedding indexTask(Task task, String projectName, User user) {
        if (task == null || user == null) return null;

        StringBuilder text = new StringBuilder();
        text.append("TASK DETAILS:\n");
        text.append("Task Title: ").append(task.getTitle()).append("\n");
        if (projectName != null) text.append("Project Name: ").append(projectName).append("\n");
        text.append("Status: ").append(task.getStatus()).append("\n");
        text.append("Priority: ").append(task.getPriority()).append("\n");
        if (task.getDueDate() != null) text.append("Due Date: ").append(task.getDueDate()).append("\n");
        if (task.getEstimatedHours() != null) text.append("Estimated Hours: ").append(task.getEstimatedHours()).append("\n");
        if (task.getDescription() != null) text.append("Description: ").append(task.getDescription()).append("\n");

        return saveOrUpdateEmbedding(user, "TASK", task.getId(), text.toString());
    }

    @Transactional
    public void removeEntityEmbedding(UUID userId, String entityType, UUID entityId) {
        vectorEmbeddingRepository.deleteByUserIdAndEntityTypeAndEntityId(userId, entityType, entityId);
    }

    private VectorEmbedding saveOrUpdateEmbedding(User user, String entityType, UUID entityId, String content) {
        float[] vector = embeddingService.generateEmbedding(content);
        String jsonVector = embeddingService.serializeVector(vector);

        Optional<VectorEmbedding> existing = vectorEmbeddingRepository
                .findByUserIdAndEntityTypeAndEntityId(user.getId(), entityType, entityId);

        VectorEmbedding embedding;
        if (existing.isPresent()) {
            embedding = existing.get();
            embedding.setContent(content);
            embedding.setEmbeddingJson(jsonVector);
        } else {
            embedding = VectorEmbedding.builder()
                    .user(user)
                    .entityType(entityType)
                    .entityId(entityId)
                    .content(content)
                    .embeddingJson(jsonVector)
                    .build();
        }

        return vectorEmbeddingRepository.save(embedding);
    }
}
