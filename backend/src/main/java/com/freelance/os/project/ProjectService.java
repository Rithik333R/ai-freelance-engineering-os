package com.freelance.os.project;

import com.freelance.os.client.Client;
import com.freelance.os.client.ClientRepository;
import com.freelance.os.common.dto.PagedResponse;
import com.freelance.os.common.exception.ResourceNotFoundException;
import com.freelance.os.common.exception.UnauthorizedAccessException;
import com.freelance.os.project.dto.ProjectRequest;
import com.freelance.os.project.dto.ProjectResponse;
import com.freelance.os.user.User;
import com.freelance.os.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));
            if (!client.getUser().getId().equals(userId)) {
                throw new UnauthorizedAccessException("Client does not belong to the authenticated user");
            }
        }

        Project project = Project.builder()
                .user(user)
                .client(client)
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .budget(request.getBudget())
                .startDate(request.getStartDate())
                .targetEndDate(request.getTargetEndDate())
                .build();

        Project savedProject = projectRepository.save(project);
        return mapToResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsForUser(UUID userId) {
        return projectRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> getProjectsForUserPaginated(UUID userId, Pageable pageable) {
        int cappedSize = Math.min(pageable.getPageSize(), 100);
        Pageable cappedPageable = PageRequest.of(pageable.getPageNumber(), cappedSize, pageable.getSort());
        Page<ProjectResponse> page = projectRepository.findByUserId(userId, cappedPageable)
                .map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId, UUID userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectRequest request, UUID userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));
            if (!client.getUser().getId().equals(userId)) {
                throw new UnauthorizedAccessException("Client does not belong to the authenticated user");
            }
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setClient(client);
        project.setBudget(request.getBudget());
        project.setStartDate(request.getStartDate());
        project.setTargetEndDate(request.getTargetEndDate());

        Project updatedProject = projectRepository.save(project);
        return mapToResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .clientId(project.getClient() != null ? project.getClient().getId() : null)
                .clientName(project.getClient() != null ? project.getClient().getCompanyName() : null)
                .budget(project.getBudget())
                .startDate(project.getStartDate())
                .targetEndDate(project.getTargetEndDate())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
