package com.freelance.os.resource;

import com.freelance.os.auth.AuthService;
import com.freelance.os.auth.dto.AuthResponse;
import com.freelance.os.auth.dto.RegisterRequest;
import com.freelance.os.client.ClientService;
import com.freelance.os.client.dto.ClientRequest;
import com.freelance.os.client.dto.ClientResponse;
import com.freelance.os.common.dto.PagedResponse;
import com.freelance.os.common.exception.ResourceNotFoundException;
import com.freelance.os.common.exception.UnauthorizedAccessException;
import com.freelance.os.project.ProjectService;
import com.freelance.os.project.dto.ProjectRequest;
import com.freelance.os.project.dto.ProjectResponse;
import com.freelance.os.task.TaskService;
import com.freelance.os.task.dto.TaskRequest;
import com.freelance.os.task.dto.TaskResponse;
import com.freelance.os.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResourceAuthorizationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    private UUID user1Id;
    private UUID user2Id;

    @BeforeEach
    void setUp() {
        AuthResponse user1Auth = authService.register(RegisterRequest.builder()
                .email("user1@example.com")
                .password("Password123!")
                .fullName("User One")
                .role(Role.ROLE_FREELANCER)
                .build());
        user1Id = user1Auth.getUser().getId();

        AuthResponse user2Auth = authService.register(RegisterRequest.builder()
                .email("user2@example.com")
                .password("Password123!")
                .fullName("User Two")
                .role(Role.ROLE_FREELANCER)
                .build());
        user2Id = user2Auth.getUser().getId();
    }

    // 1. Create client
    @Test
    void test1_CreateClient() {
        ClientRequest request = ClientRequest.builder()
                .companyName("Acme Corp")
                .contactEmail("contact@acme.com")
                .phone("123-456-7890")
                .notes("Primary client")
                .build();

        ClientResponse response = clientService.createClient(request, user1Id);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Acme Corp", response.getCompanyName());
    }

    // 2. Retrieve own client
    @Test
    void test2_RetrieveOwnClient() {
        ClientResponse created = clientService.createClient(ClientRequest.builder()
                .companyName("Globex Corp")
                .build(), user1Id);

        ClientResponse retrieved = clientService.getClientById(created.getId(), user1Id);

        assertNotNull(retrieved);
        assertEquals("Globex Corp", retrieved.getCompanyName());
    }

    // 3. Update own client
    @Test
    void test3_UpdateOwnClient() {
        ClientResponse created = clientService.createClient(ClientRequest.builder()
                .companyName("Initech")
                .build(), user1Id);

        ClientRequest updateRequest = ClientRequest.builder()
                .companyName("Initech Updated")
                .contactEmail("updated@initech.com")
                .build();

        ClientResponse updated = clientService.updateClient(created.getId(), updateRequest, user1Id);

        assertEquals("Initech Updated", updated.getCompanyName());
        assertEquals("updated@initech.com", updated.getContactEmail());
    }

    // 4. Delete own client
    @Test
    void test4_DeleteOwnClient() {
        ClientResponse created = clientService.createClient(ClientRequest.builder()
                .companyName("Umbrella Corp")
                .build(), user1Id);

        clientService.deleteClient(created.getId(), user1Id);

        assertThrows(ResourceNotFoundException.class, () -> clientService.getClientById(created.getId(), user1Id));
    }

    // 5. User cannot access another user's client
    @Test
    void test5_UserCannotAccessAnotherUsersClient() {
        ClientResponse user1Client = clientService.createClient(ClientRequest.builder()
                .companyName("User1 Secret Client")
                .build(), user1Id);

        assertThrows(ResourceNotFoundException.class, () -> clientService.getClientById(user1Client.getId(), user2Id));
        assertThrows(ResourceNotFoundException.class, () -> clientService.updateClient(user1Client.getId(), ClientRequest.builder().companyName("Hacked").build(), user2Id));
        assertThrows(ResourceNotFoundException.class, () -> clientService.deleteClient(user1Client.getId(), user2Id));
    }

    // 6. Create project
    @Test
    void test6_CreateProject() {
        ClientResponse client = clientService.createClient(ClientRequest.builder()
                .companyName("Stark Industries")
                .build(), user1Id);

        ProjectRequest request = ProjectRequest.builder()
                .name("Jarvis AI Project")
                .description("Next-gen AI assistant")
                .status("IN_PROGRESS")
                .clientId(client.getId())
                .budget(new BigDecimal("50000.00"))
                .startDate(LocalDate.now())
                .targetEndDate(LocalDate.now().plusMonths(3))
                .build();

        ProjectResponse response = projectService.createProject(request, user1Id);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Jarvis AI Project", response.getName());
        assertEquals(client.getId(), response.getClientId());
    }

    // 7. User cannot access another user's project
    @Test
    void test7_UserCannotAccessAnotherUsersProject() {
        ProjectResponse user1Project = projectService.createProject(ProjectRequest.builder()
                .name("User1 Secret Project")
                .status("DRAFT")
                .build(), user1Id);

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(user1Project.getId(), user2Id));
        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(user1Project.getId(), ProjectRequest.builder().name("Hacked").status("DONE").build(), user2Id));
        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(user1Project.getId(), user2Id));
    }

    // 8. User cannot use another user's client
    @Test
    void test8_UserCannotUseAnotherUsersClient() {
        ClientResponse user1Client = clientService.createClient(ClientRequest.builder()
                .companyName("User1 Exclusive Client")
                .build(), user1Id);

        ProjectRequest projectRequest = ProjectRequest.builder()
                .name("User2 Unauthorized Project")
                .status("PLANNING")
                .clientId(user1Client.getId())
                .build();

        assertThrows(UnauthorizedAccessException.class, () -> projectService.createProject(projectRequest, user2Id));
    }

    // 9. Create task
    @Test
    void test9_CreateTask() {
        ProjectResponse project = projectService.createProject(ProjectRequest.builder()
                .name("Website Redesign")
                .status("IN_PROGRESS")
                .build(), user1Id);

        TaskRequest taskRequest = TaskRequest.builder()
                .title("Design Figma Mockups")
                .description("Create dark mode UI")
                .status("TODO")
                .priority("HIGH")
                .estimatedHours(16)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        TaskResponse response = taskService.createTask(project.getId(), taskRequest, user1Id);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Design Figma Mockups", response.getTitle());
        assertEquals(project.getId(), response.getProjectId());
    }

    // 10. User cannot create/access tasks under another user's project
    @Test
    void test10_UserCannotCreateOrAccessTasksUnderAnotherUsersProject() {
        ProjectResponse user1Project = projectService.createProject(ProjectRequest.builder()
                .name("User1 Secure Project")
                .status("IN_PROGRESS")
                .build(), user1Id);

        TaskResponse user1Task = taskService.createTask(user1Project.getId(), TaskRequest.builder()
                .title("User1 Internal Task")
                .status("TODO")
                .priority("MEDIUM")
                .build(), user1Id);

        TaskRequest hackTaskRequest = TaskRequest.builder()
                .title("User2 Injected Task")
                .status("TODO")
                .priority("HIGH")
                .build();

        assertThrows(UnauthorizedAccessException.class, () -> taskService.createTask(user1Project.getId(), hackTaskRequest, user2Id));
        assertThrows(UnauthorizedAccessException.class, () -> taskService.getTasksByProject(user1Project.getId(), user2Id));
        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(user1Task.getId(), TaskRequest.builder().title("Hacked").status("DONE").priority("LOW").build(), user2Id));
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(user1Task.getId(), user2Id));
    }

    // 11. Pagination Test
    @Test
    void test11_PaginationAndSorting() {
        for (int i = 1; i <= 5; i++) {
            clientService.createClient(ClientRequest.builder()
                    .companyName("Client " + i)
                    .build(), user1Id);
        }

        Pageable pageable = PageRequest.of(0, 2);
        PagedResponse<ClientResponse> pagedResponse = clientService.getClientsForUserPaginated(user1Id, pageable);

        assertNotNull(pagedResponse);
        assertEquals(2, pagedResponse.getContent().size());
        assertEquals(5, pagedResponse.getTotalElements());
        assertEquals(3, pagedResponse.getTotalPages());
        assertFalse(pagedResponse.isLast());
    }
}
