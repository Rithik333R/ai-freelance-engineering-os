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
    void testBuildContext_ContainsOnlyAuthenticatedUsersData() {
        // User 1 setup
        ClientResponse client1 = clientService.createClient(ClientRequest.builder()
                .companyName("User1 Alpha Corp")
                .contactEmail("alpha@user1.com")
                .phone("111-222-3333")
                .build(), user1Id);

        ProjectResponse project1 = projectService.createProject(ProjectRequest.builder()
                .name("User1 Alpha Project")
                .status("IN_PROGRESS")
                .clientId(client1.getId())
                .budget(new BigDecimal("15000.00"))
                .build(), user1Id);

        taskService.createTask(project1.getId(), TaskRequest.builder()
                .title("User1 Alpha Task")
                .status("TODO")
                .priority("HIGH")
                .estimatedHours(10)
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
