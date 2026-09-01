package com.freelance.os.ai;

import com.freelance.os.auth.AuthService;
import com.freelance.os.auth.dto.AuthResponse;
import com.freelance.os.auth.dto.RegisterRequest;
import com.freelance.os.client.ClientService;
import com.freelance.os.client.dto.ClientRequest;
import com.freelance.os.client.dto.ClientResponse;
import com.freelance.os.project.ProjectService;
import com.freelance.os.project.dto.ProjectRequest;
import com.freelance.os.project.dto.ProjectResponse;
import com.freelance.os.task.TaskService;
import com.freelance.os.task.dto.TaskRequest;
import com.freelance.os.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiContextBuilderTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AiContextBuilder aiContextBuilder;

    private UUID user1Id;
    private UUID user2Id;

    @BeforeEach
    void setUp() {
        AuthResponse user1Auth = authService.register(RegisterRequest.builder()
                .email("aiuser1@example.com")
                .password("Password123!")
                .fullName("AI User One")
                .role(Role.ROLE_FREELANCER)
                .build());
        user1Id = user1Auth.getUser().getId();

        AuthResponse user2Auth = authService.register(RegisterRequest.builder()
                .email("aiuser2@example.com")
                .password("Password123!")
                .fullName("AI User Two")
                .role(Role.ROLE_FREELANCER)
                .build());
        user2Id = user2Auth.getUser().getId();
    }

    @Test
    void testBuildContext_CompleteContext_RendersAllMetadataFields() {
        ClientResponse client = clientService.createClient(ClientRequest.builder()
                .companyName("Acme Global")
                .contactEmail("contact@acme.com")
                .phone("555-0199")
                .notes("Key enterprise account")
                .build(), user1Id);

        ProjectResponse project = projectService.createProject(ProjectRequest.builder()
                .name("E-Commerce Portal")
                .description("Next-gen e-commerce storefront redesign")
                .status("IN_PROGRESS")
                .clientId(client.getId())
                .budget(new BigDecimal("25000.00"))
                .startDate(LocalDate.of(2026, 1, 15))
                .targetEndDate(LocalDate.of(2026, 6, 30))
                .build(), user1Id);

        taskService.createTask(project.getId(), TaskRequest.builder()
                .title("Design Database Schema")
                .description("Create PostgreSQL ERD and Flyway migrations")
                .status("COMPLETED")
                .priority("HIGH")
                .estimatedHours(16)
                .dueDate(LocalDate.of(2026, 2, 1))
                .build(), user1Id);

        String context = aiContextBuilder.buildContext(user1Id);

        assertNotNull(context);
        assertTrue(context.contains("Acme Global"));
        assertTrue(context.contains("contact@acme.com"));
        assertTrue(context.contains("555-0199"));
        assertTrue(context.contains("Key enterprise account"));
        assertTrue(context.contains("E-Commerce Portal"));
        assertTrue(context.contains("Next-gen e-commerce storefront redesign"));
        assertTrue(context.contains("IN_PROGRESS"));
        assertTrue(context.contains("$25000.00"));
        assertTrue(context.contains("2026-01-15"));
        assertTrue(context.contains("2026-06-30"));
        assertTrue(context.contains("Design Database Schema"));
        assertTrue(context.contains("Create PostgreSQL ERD and Flyway migrations"));
        assertTrue(context.contains("COMPLETED"));
        assertTrue(context.contains("HIGH"));
        assertTrue(context.contains("16h"));
        assertTrue(context.contains("2026-02-01"));
    }

    @Test
    void testBuildContext_MultipleProjectsAndMultipleTasks() {
        ClientResponse client = clientService.createClient(ClientRequest.builder()
                .companyName("Multi Project Client")
                .build(), user1Id);

        ProjectResponse p1 = projectService.createProject(ProjectRequest.builder()
                .name("Project Alpha")
                .status("IN_PROGRESS")
                .clientId(client.getId())
                .build(), user1Id);

        ProjectResponse p2 = projectService.createProject(ProjectRequest.builder()
                .name("Project Beta")
                .status("PLANNING")
                .clientId(client.getId())
                .build(), user1Id);

        taskService.createTask(p1.getId(), TaskRequest.builder().title("P1 Task A").status("TODO").priority("MEDIUM").build(), user1Id);
        taskService.createTask(p1.getId(), TaskRequest.builder().title("P1 Task B").status("IN_PROGRESS").priority("HIGH").build(), user1Id);

        taskService.createTask(p2.getId(), TaskRequest.builder().title("P2 Task X").status("TODO").priority("LOW").build(), user1Id);

        String context = aiContextBuilder.buildContext(user1Id);

        assertTrue(context.contains("Projects (Total: 2)"));
        assertTrue(context.contains("Project Alpha"));
        assertTrue(context.contains("Project Beta"));
        assertTrue(context.contains("Tasks (Total: 2)"));
        assertTrue(context.contains("P1 Task A"));
        assertTrue(context.contains("P1 Task B"));
        assertTrue(context.contains("Tasks (Total: 1)"));
        assertTrue(context.contains("P2 Task X"));
    }

    @Test
    void testBuildContext_ContainsOnlyAuthenticatedUsersData() {
        // User 1 setup
        ClientResponse client1 = clientService.createClient(ClientRequest.builder()
                .companyName("User1 Alpha Corp")
                .contactEmail("alpha@user1.com")
                .build(), user1Id);

        ProjectResponse project1 = projectService.createProject(ProjectRequest.builder()
                .name("User1 Alpha Project")
                .status("IN_PROGRESS")
                .clientId(client1.getId())
                .build(), user1Id);

        taskService.createTask(project1.getId(), TaskRequest.builder()
                .title("User1 Alpha Task")
                .status("TODO")
                .priority("HIGH")
                .build(), user1Id);

        // User 2 setup
        ClientResponse client2 = clientService.createClient(ClientRequest.builder()
                .companyName("User2 Beta Corp")
                .contactEmail("beta@user2.com")
                .build(), user2Id);

        ProjectResponse project2 = projectService.createProject(ProjectRequest.builder()
                .name("User2 Beta Project")
                .status("PLANNING")
                .clientId(client2.getId())
                .build(), user2Id);

        taskService.createTask(project2.getId(), TaskRequest.builder()
                .title("User2 Beta Task")
                .status("IN_PROGRESS")
                .priority("LOW")
                .build(), user2Id);

        // Build context for User 1
        String user1Context = aiContextBuilder.buildContext(user1Id);

        assertNotNull(user1Context);
        assertTrue(user1Context.contains("User1 Alpha Corp"));
        assertTrue(user1Context.contains("User1 Alpha Project"));
        assertTrue(user1Context.contains("User1 Alpha Task"));

        // Critical isolation assertion: another user's data must NOT enter the AI context
        assertFalse(user1Context.contains("User2 Beta Corp"));
        assertFalse(user1Context.contains("User2 Beta Project"));
        assertFalse(user1Context.contains("User2 Beta Task"));

        // Build context for User 2
        String user2Context = aiContextBuilder.buildContext(user2Id);

        assertNotNull(user2Context);
        assertTrue(user2Context.contains("User2 Beta Corp"));
        assertTrue(user2Context.contains("User2 Beta Project"));
        assertTrue(user2Context.contains("User2 Beta Task"));

        assertFalse(user2Context.contains("User1 Alpha Corp"));
        assertFalse(user2Context.contains("User1 Alpha Project"));
        assertFalse(user2Context.contains("User1 Alpha Task"));
    }

    @Test
    void testBuildContext_EmptyUserContext_RendersClearNoneRepresentation() {
        String emptyContext = aiContextBuilder.buildContext(user2Id);

        assertNotNull(emptyContext);
        assertTrue(emptyContext.contains("Clients: None"));
        assertTrue(emptyContext.contains("Projects: None"));
    }

    @Test
    void testBuildContext_DoesNotLeakSensitiveAuthInfo() {
        clientService.createClient(ClientRequest.builder()
                .companyName("Secure Corp")
                .build(), user1Id);

        String context = aiContextBuilder.buildContext(user1Id);

        assertFalse(context.contains("Password123!"));
        assertFalse(context.contains("accessToken"));
        assertFalse(context.contains("refreshToken"));
        assertFalse(context.contains("ROLE_FREELANCER"));
    }
}
