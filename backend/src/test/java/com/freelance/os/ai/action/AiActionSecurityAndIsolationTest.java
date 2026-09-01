package com.freelance.os.ai.action;

import com.freelance.os.ai.action.dto.AiActionExecutionRequest;
import com.freelance.os.ai.action.dto.AiActionExecutionResult;
import com.freelance.os.ai.action.enums.AiActionStatus;
import com.freelance.os.ai.action.enums.AiActionType;
import com.freelance.os.ai.action.service.AiActionExecutor;
import com.freelance.os.client.dto.ClientRequest;
import com.freelance.os.project.ProjectService;
import com.freelance.os.project.dto.ProjectRequest;
import com.freelance.os.project.dto.ProjectResponse;
import com.freelance.os.task.dto.TaskRequest;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiActionSecurityAndIsolationTest {

    @Autowired
    private AiActionExecutor aiActionExecutor;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.builder()
                .email("usera_action_" + UUID.randomUUID() + "@test.com")
                .fullName("User A Action")
                .passwordHash("hash")
                .role(Role.ROLE_FREELANCER)
                .build());

        userB = userRepository.save(User.builder()
                .email("userb_action_" + UUID.randomUUID() + "@test.com")
                .fullName("User B Action")
                .passwordHash("hash")
                .role(Role.ROLE_FREELANCER)
                .build());
    }

    @Test
    @DisplayName("Should successfully create client for User A when confirmed")
    void executeAction_CreateClientSuccess() {
        AiActionExecutionRequest request = AiActionExecutionRequest.builder()
                .actionType(AiActionType.CREATE_CLIENT)
                .clientPayload(ClientRequest.builder()
                        .companyName("User A Enterprise")
                        .contactEmail("usera@enterprise.com")
                        .build())
                .confirmed(true)
                .build();

        AiActionExecutionResult result = aiActionExecutor.executeAction(request, userA.getId());

        assertEquals(AiActionStatus.EXECUTED, result.getStatus());
        assertNotNull(result.getCreatedResourceId());
        assertNotNull(result.getClientResponse());
        assertEquals("User A Enterprise", result.getClientResponse().getCompanyName());
    }

    @Test
    @DisplayName("Security Isolation: User A cannot create a task in User B's project")
    void executeAction_PreventUserACreatingTaskInUserBProject() {
        // Create Project owned by User B
        ProjectResponse userBProject = projectService.createProject(ProjectRequest.builder()
                .name("User B Confidential Project")
                .status("PLANNING")
                .build(), userB.getId());

        // User A attempts to create a task in User B's project
        AiActionExecutionRequest request = AiActionExecutionRequest.builder()
                .actionType(AiActionType.CREATE_TASK)
                .projectId(userBProject.getId())
                .taskPayload(TaskRequest.builder()
                        .title("Malicious Inject Task")
                        .status("TODO")
                        .priority("HIGH")
                        .build())
                .confirmed(true)
                .build();

        AiActionExecutionResult result = aiActionExecutor.executeAction(request, userA.getId());

        assertEquals(AiActionStatus.FAILED, result.getStatus());
        assertTrue(result.getMessage().contains("Project does not belong to the authenticated user"));
    }

    @Test
    @DisplayName("Security Assertion: Unconfirmed actions must be CANCELLED without resource creation")
    void executeAction_UnconfirmedActionIsCancelled() {
        AiActionExecutionRequest request = AiActionExecutionRequest.builder()
                .actionType(AiActionType.CREATE_CLIENT)
                .clientPayload(ClientRequest.builder()
                        .companyName("Never Created Corp")
                        .build())
                .confirmed(false)
                .build();

        AiActionExecutionResult result = aiActionExecutor.executeAction(request, userA.getId());

        assertEquals(AiActionStatus.CANCELLED, result.getStatus());
        assertNull(result.getCreatedResourceId());
    }
}
