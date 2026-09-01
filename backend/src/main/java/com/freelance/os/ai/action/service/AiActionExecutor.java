package com.freelance.os.ai.action.service;

import com.freelance.os.ai.action.dto.AiActionExecutionRequest;
import com.freelance.os.ai.action.dto.AiActionExecutionResult;
import com.freelance.os.ai.action.enums.AiActionStatus;
import com.freelance.os.client.ClientService;
import com.freelance.os.client.dto.ClientResponse;
import com.freelance.os.common.exception.UnauthorizedAccessException;
import com.freelance.os.project.ProjectService;
import com.freelance.os.project.dto.ProjectResponse;
import com.freelance.os.task.TaskService;
import com.freelance.os.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiActionExecutor {

    private final ClientService clientService;
    private final ProjectService projectService;
    private final TaskService taskService;

    @Transactional
    public AiActionExecutionResult executeAction(AiActionExecutionRequest request, UUID userId) {
        if (userId == null) {
            throw new UnauthorizedAccessException("Authentication required to execute actions");
        }

        if (!request.isConfirmed()) {
            return AiActionExecutionResult.builder()
                    .actionType(request.getActionType())
                    .status(AiActionStatus.CANCELLED)
                    .message("Action execution cancelled. Explicit user confirmation is required.")
                    .build();
        }

        try {
            return switch (request.getActionType()) {
                case CREATE_CLIENT -> executeCreateClient(request, userId);
                case CREATE_PROJECT -> executeCreateProject(request, userId);
                case CREATE_TASK -> executeCreateTask(request, userId);
            };
        } catch (Exception ex) {
            return AiActionExecutionResult.builder()
                    .actionType(request.getActionType())
                    .status(AiActionStatus.FAILED)
                    .message("Failed to execute action: " + ex.getMessage())
                    .build();
        }
    }

    private AiActionExecutionResult executeCreateClient(AiActionExecutionRequest request, UUID userId) {
        if (request.getClientPayload() == null || request.getClientPayload().getCompanyName() == null) {
            throw new IllegalArgumentException("Company name is required for creating a client");
        }

        ClientResponse response = clientService.createClient(request.getClientPayload(), userId);

        return AiActionExecutionResult.builder()
                .actionType(request.getActionType())
                .status(AiActionStatus.EXECUTED)
                .message("Successfully created client: " + response.getCompanyName())
                .createdResourceId(response.getId())
                .clientResponse(response)
                .build();
    }

    private AiActionExecutionResult executeCreateProject(AiActionExecutionRequest request, UUID userId) {
        if (request.getProjectPayload() == null || request.getProjectPayload().getName() == null) {
            throw new IllegalArgumentException("Project name is required for creating a project");
        }

        ProjectResponse response = projectService.createProject(request.getProjectPayload(), userId);

        return AiActionExecutionResult.builder()
                .actionType(request.getActionType())
                .status(AiActionStatus.EXECUTED)
                .message("Successfully created project: " + response.getName())
                .createdResourceId(response.getId())
                .projectResponse(response)
                .build();
    }

    private AiActionExecutionResult executeCreateTask(AiActionExecutionRequest request, UUID userId) {
        if (request.getTaskPayload() == null || request.getTaskPayload().getTitle() == null) {
            throw new IllegalArgumentException("Task title is required for creating a task");
        }

        UUID targetProjectId = request.getProjectId();
        if (targetProjectId == null) {
            List<ProjectResponse> projects = projectService.getProjectsForUser(userId);
            if (projects.isEmpty()) {
                throw new IllegalArgumentException("Cannot create task without an existing project. Please create a project first.");
            }
            targetProjectId = projects.get(0).getId();
        }

        TaskResponse response = taskService.createTask(targetProjectId, request.getTaskPayload(), userId);

        return AiActionExecutionResult.builder()
                .actionType(request.getActionType())
                .status(AiActionStatus.EXECUTED)
                .message("Successfully created task: " + response.getTitle())
                .createdResourceId(response.getId())
                .taskResponse(response)
                .build();
    }
}
