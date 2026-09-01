package com.freelance.os.ai.action;

import com.freelance.os.ai.action.dto.AiActionExecutionRequest;
import com.freelance.os.ai.action.dto.AiActionExecutionResult;
import com.freelance.os.ai.action.enums.AiActionStatus;
import com.freelance.os.ai.action.enums.AiActionType;
import com.freelance.os.ai.action.service.AiActionExecutor;
import com.freelance.os.client.ClientService;
import com.freelance.os.client.dto.ClientRequest;
import com.freelance.os.client.dto.ClientResponse;
import com.freelance.os.common.exception.UnauthorizedAccessException;
import com.freelance.os.project.ProjectService;
import com.freelance.os.task.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiActionExecutorTest {

    @Mock
    private ClientService clientService;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskService taskService;

    private AiActionExecutor executor;
    private UUID userId;

    @BeforeEach
    void setUp() {
        executor = new AiActionExecutor(clientService, projectService, taskService);
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should execute CREATE_CLIENT action when confirmed by user")
    void executeAction_CreateClientConfirmed() {
        ClientRequest clientReq = ClientRequest.builder().companyName("Acme Corp").build();
        ClientResponse clientRes = ClientResponse.builder().id(UUID.randomUUID()).companyName("Acme Corp").build();

        when(clientService.createClient(any(), eq(userId))).thenReturn(clientRes);

        AiActionExecutionRequest request = AiActionExecutionRequest.builder()
                .actionType(AiActionType.CREATE_CLIENT)
                .clientPayload(clientReq)
                .confirmed(true)
                .build();

        AiActionExecutionResult result = executor.executeAction(request, userId);

        assertEquals(AiActionStatus.EXECUTED, result.getStatus());
        assertEquals(clientRes.getId(), result.getCreatedResourceId());
        verify(clientService, times(1)).createClient(any(), eq(userId));
    }

    @Test
    @DisplayName("Security Assertion: Should cancel action if user has NOT confirmed")
    void executeAction_UnconfirmedShouldCancel() {
        AiActionExecutionRequest request = AiActionExecutionRequest.builder()
                .actionType(AiActionType.CREATE_CLIENT)
                .clientPayload(ClientRequest.builder().companyName("Unconfirmed Corp").build())
                .confirmed(false)
                .build();

        AiActionExecutionResult result = executor.executeAction(request, userId);

        assertEquals(AiActionStatus.CANCELLED, result.getStatus());
        assertTrue(result.getMessage().contains("Explicit user confirmation is required"));
        verifyNoInteractions(clientService);
    }

    @Test
    @DisplayName("Security Assertion: Should throw exception if userId is null")
    void executeAction_NullUserIdThrowsUnauthorized() {
        AiActionExecutionRequest request = AiActionExecutionRequest.builder()
                .actionType(AiActionType.CREATE_CLIENT)
                .confirmed(true)
                .build();

        assertThrows(UnauthorizedAccessException.class, () -> executor.executeAction(request, null));
    }
}
