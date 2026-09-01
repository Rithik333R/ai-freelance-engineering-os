package com.freelance.os.ai;

import com.freelance.os.ai.service.VectorSearchService;
import com.freelance.os.client.ClientService;
import com.freelance.os.client.dto.ClientResponse;
import com.freelance.os.project.ProjectService;
import com.freelance.os.project.dto.ProjectResponse;
import com.freelance.os.task.TaskService;
import com.freelance.os.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AiContextBuilder {

    private final ClientService clientService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final VectorSearchService vectorSearchService;

    public String buildContext(UUID userId) {
        return buildContext(userId, null);
    }

    public String buildContext(UUID userId, String userQuery) {
        StringBuilder sb = new StringBuilder();

        // Include semantic vector retrieval context if available
        if (userQuery != null && !userQuery.trim().isEmpty() && vectorSearchService != null) {
            String semanticContext = vectorSearchService.buildSemanticContext(userId, userQuery);
            if (semanticContext != null && !semanticContext.trim().isEmpty()) {
                sb.append(semanticContext).append("\n");
            }
        }

        sb.append("=== USER APPLICATION CONTEXT ===\n");

        // 1. Clients Section
        List<ClientResponse> clients = clientService.getClientsForUser(userId);
        if (clients.isEmpty()) {
            sb.append("Clients: None\n");
        } else {
            sb.append("Clients (Total: ").append(clients.size()).append("):\n");
            for (ClientResponse client : clients) {
                sb.append(" - [Client ID: ").append(client.getId()).append("]")
                  .append(" Company: ").append(client.getCompanyName());
                if (client.getContactEmail() != null && !client.getContactEmail().isBlank()) {
                    sb.append(" | Email: ").append(client.getContactEmail());
                }
                if (client.getPhone() != null && !client.getPhone().isBlank()) {
                    sb.append(" | Phone: ").append(client.getPhone());
                }
                if (client.getNotes() != null && !client.getNotes().isBlank()) {
                    sb.append(" | Notes: ").append(client.getNotes());
                }
                sb.append("\n");
            }
        }

        // 2. Projects & Tasks Section
        List<ProjectResponse> projects = projectService.getProjectsForUser(userId);
        if (projects.isEmpty()) {
            sb.append("Projects: None\n");
        } else {
            sb.append("Projects (Total: ").append(projects.size()).append("):\n");
            for (ProjectResponse project : projects) {
                sb.append(" - [Project ID: ").append(project.getId()).append("]")
                  .append(" Name: ").append(project.getName())
                  .append(" | Status: ").append(project.getStatus());

                if (project.getClientName() != null && !project.getClientName().isBlank()) {
                    sb.append(" | Client: ").append(project.getClientName());
                }
                if (project.getBudget() != null) {
                    sb.append(" | Budget: $").append(project.getBudget());
                }
                if (project.getStartDate() != null) {
                    sb.append(" | Start Date: ").append(project.getStartDate());
                }
                if (project.getTargetEndDate() != null) {
                    sb.append(" | Target End Date: ").append(project.getTargetEndDate());
                }
                sb.append("\n");

                if (project.getDescription() != null && !project.getDescription().isBlank()) {
                    sb.append("   Description: ").append(project.getDescription()).append("\n");
                }

                // Project Tasks
                List<TaskResponse> tasks = taskService.getTasksByProject(project.getId(), userId);
                if (tasks.isEmpty()) {
                    sb.append("   Tasks: None\n");
                } else {
                    sb.append("   Tasks (Total: ").append(tasks.size()).append("):\n");
                    for (TaskResponse task : tasks) {
                        sb.append("   - [Task ID: ").append(task.getId()).append("]")
                          .append(" Title: ").append(task.getTitle())
                          .append(" | Status: ").append(task.getStatus())
                          .append(" | Priority: ").append(task.getPriority());

                        if (task.getEstimatedHours() != null) {
                            sb.append(" | Est Hours: ").append(task.getEstimatedHours()).append("h");
                        }
                        if (task.getDueDate() != null) {
                            sb.append(" | Due Date: ").append(task.getDueDate());
                        }
                        sb.append("\n");

                        if (task.getDescription() != null && !task.getDescription().isBlank()) {
                            sb.append("     Description: ").append(task.getDescription()).append("\n");
                        }
                    }
                }
            }
        }

        sb.append("=================================");
        return sb.toString();
    }
}
