package com.freelance.os.ai;

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

    public String buildContext(UUID userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== USER APPLICATION CONTEXT ===\n");

        List<ClientResponse> clients = clientService.getClientsForUser(userId);
        if (clients.isEmpty()) {
            sb.append("Clients: None\n");
        } else {
            sb.append("Clients (").append(clients.size()).append("):\n");
            for (ClientResponse client : clients) {
                sb.append(" - Client ID: ").append(client.getId())
                  .append(", Company Name: ").append(client.getCompanyName());
                if (client.getContactEmail() != null) {
                    sb.append(", Email: ").append(client.getContactEmail());
                }
                if (client.getPhone() != null) {
                    sb.append(", Phone: ").append(client.getPhone());
                }
                sb.append("\n");
            }
        }

        List<ProjectResponse> projects = projectService.getProjectsForUser(userId);
        if (projects.isEmpty()) {
            sb.append("Projects: None\n");
        } else {
            sb.append("Projects (").append(projects.size()).append("):\n");
            for (ProjectResponse project : projects) {
                sb.append(" - Project ID: ").append(project.getId())
                  .append(", Name: ").append(project.getName())
                  .append(", Status: ").append(project.getStatus());
                if (project.getClientName() != null) {
                    sb.append(", Client: ").append(project.getClientName());
                }
                if (project.getBudget() != null) {
                    sb.append(", Budget: ").append(project.getBudget());
                }
                if (project.getStartDate() != null) {
                    sb.append(", Start Date: ").append(project.getStartDate());
                }
                if (project.getTargetEndDate() != null) {
                    sb.append(", Target End Date: ").append(project.getTargetEndDate());
                }
                sb.append("\n");

                List<TaskResponse> tasks = taskService.getTasksByProject(project.getId(), userId);
                if (tasks.isEmpty()) {
                    sb.append("   Tasks: None\n");
                } else {
                    sb.append("   Tasks (").append(tasks.size()).append("):\n");
                    for (TaskResponse task : tasks) {
                        sb.append("   - Task ID: ").append(task.getId())
                          .append(", Title: ").append(task.getTitle())
                          .append(", Status: ").append(task.getStatus())
                          .append(", Priority: ").append(task.getPriority());
                        if (task.getEstimatedHours() != null) {
                            sb.append(", Estimated Hours: ").append(task.getEstimatedHours());
                        }
                        if (task.getDueDate() != null) {
                            sb.append(", Due Date: ").append(task.getDueDate());
                        }
                        sb.append("\n");
                    }
                }
            }
        }

        sb.append("=================================");
        return sb.toString();
    }
}
